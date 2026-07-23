package com.savemyseat.repository;


import com.savemyseat.user.User;
import com.savemyseat.user.UserRepository;
import com.savemyseat.venue.Venue;
import com.savemyseat.user.Role;
import com.savemyseat.venue.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class VenueRepositoryTest {
  @Container
  static PostgreSQLContainer<?> postgres =
          new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry){
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  VenueRepository venueRepository;
  @Autowired
  UserRepository userRepository;

  @Test
  void savesAndReadsBackVenue(){
    User u = new User("Jack", "wade", "123@example.com", "123",
            Role.ORGANIZER);
    User organizer = userRepository.save(u);
    Venue v = new Venue(organizer, "bar", "Descritpion",
            "shadowbrook", "Oak " +
            "Harbor",
            "Washington"
            , "98277");
    Venue saved = venueRepository.save(v);


    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();

    Venue fetched =
            venueRepository.findById(saved.getId()).orElseThrow();

    assertThat(fetched.getName()).isEqualTo(saved.getName());
    assertThat(fetched.getId()).isEqualTo(saved.getId());

  }

  @Test
  void  findAllVenuesByOrganizer() {
      User organizer = userRepository.save(new User("Jane",
              "Doe", "jane@example.com", "hash", Role.ORGANIZER));

      venueRepository.save(new Venue(organizer, "1 bar","description", "a",
              "b", "c", "2"));
      venueRepository.save(new Venue(organizer, "2 bar","Description","b", "c",
              "d", "3"));

      List<Venue> found = venueRepository.findByOrganizer(organizer);

      assertThat(found).hasSize(2);
  }
}
