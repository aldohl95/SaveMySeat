package com.savemyseat;

import com.savemyseat.user.Role;
import com.savemyseat.user.User;
import com.savemyseat.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ErrorStatusIntegrationTest {

    private static final String VALID_VENUE = """
            {"name":"Test Hall","streetName":"1 Main St","city":"Springfield","state":"IL","zip":"62701"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(roles = "ATTENDEE")
    void attendeeCreatingVenueGets403() throws Exception {
        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_VENUE))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizerCreatingVenueGets201() throws Exception {
        // CurrentUserProvider parses the principal as a user id, so the organizer
        // must exist in the database and the principal must be the bare id string,
        // as JwtAuthenticationFilter sets it. @WithMockUser can't do either.
        User organizer = userRepository.save(new User("Org", "Anizer",
                "org-" + UUID.randomUUID() + "@test.local", "hash", Role.ORGANIZER));
        var auth = new UsernamePasswordAuthenticationToken(
                organizer.getId().toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER")));

        mockMvc.perform(post("/api/venues")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_VENUE))
                .andExpect(status().isCreated());
    }

    @Test
    void registerWithPasswordOver72BytesGets400() throws Exception {
        String body = """
                {"firstName":"Long","lastName":"Password","email":"long-%s@test.local","password":"%s"}
                """.formatted(UUID.randomUUID(), "a".repeat(80));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
