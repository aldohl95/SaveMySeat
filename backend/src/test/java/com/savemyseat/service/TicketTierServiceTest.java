package com.savemyseat.service;

import com.savemyseat.event.Event;
import com.savemyseat.event.EventRepository;
import com.savemyseat.event.EventStatus;
import com.savemyseat.tickettier.TicketTier;
import com.savemyseat.tickettier.TicketTierRepository;
import com.savemyseat.tickettier.TicketTierService;
import com.savemyseat.tickettier.dto.TicketTierResponse;
import com.savemyseat.tickettier.dto.UpdateTicketTierRequest;
import com.savemyseat.user.Role;
import com.savemyseat.user.User;
import com.savemyseat.user.UserRepository;
import com.savemyseat.venue.Venue;
import com.savemyseat.venue.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TicketTierServiceTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres" +
            ":16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    UserRepository userRepository;
    @Autowired
    VenueRepository venueRepository;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    TicketTierRepository ticketTierRepository;
    @Autowired
    TicketTierService ticketTierService;

    @Test
    void updateChangesCapacity(){
        User u = new User("jane", "heller", "jane@example.com", "hash",
                Role.ORGANIZER);
        User organizer = userRepository.save(u);
        Venue v = new Venue(organizer, "HappyBar", "be happy", "shadowbrook",
                "oak harbor", "Washington", "98277");
        Venue venue = venueRepository.save(v);
        Event e = new Event (venue, "Bar hopping", "Hop bars at happy", OffsetDateTime.now(ZoneOffset.UTC).plusHours(1),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                EventStatus.PUBLISHED);
        Event event = eventRepository.save(e);
        TicketTier t = new TicketTier(event, "General Admission", 2500,
                100);
        TicketTier ticket = ticketTierRepository.save(t);
        UpdateTicketTierRequest update = new UpdateTicketTierRequest(
                "General Admission", 2500L, 50);

        TicketTierResponse result =
                ticketTierService.updateTicketTier(ticket.getId(), update);

        assertThat(result.capacity()).isEqualTo(50);
        assertThat(result.updatedAt()).isAfter(result.createdAt());

    }

    @Test
    void updateRejectCapacityBelowReservedPlusSold(){
        User u = new User("finn", "heller", "finn@example.com", "hash",
                Role.ORGANIZER);
        User organizer = userRepository.save(u);
        Venue v = new Venue(organizer, "Happyplay", "play happy", "shadowbrook",
                "oak harbor", "Washington", "98277");
        Venue venue = venueRepository.save(v);
        Event e = new Event (venue, "play dates", "bring yoru kids to play",
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                EventStatus.PUBLISHED);
        Event event = eventRepository.save(e);
        TicketTier t = new TicketTier(event, "General Admission", 2500,
                100);
        TicketTier ticket = ticketTierRepository.save(t);
        ticket.setSold(30);
        ticket.setReserved(60);
        ticketTierRepository.save(ticket);

        UpdateTicketTierRequest update = new UpdateTicketTierRequest(null,
                null, 50);
        assertThatThrownBy(() -> ticketTierService.updateTicketTier(ticket.getId(),
                update)).isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void updateWithNullCapacityLeavesItUnchanged() {
        User u = new User("duran", "mexi", "duran@example.com", "hash",
                Role.ORGANIZER);
        User organizer = userRepository.save(u);
        Venue v = new Venue(organizer, "BreneHouse", "place to hang with " +
                "brene",
                "shadowbrook",
                "oak harbor", "Washington", "98277");
        Venue venue = venueRepository.save(v);
        Event e = new Event (venue, "Hangout", "Hang out with brene",
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                EventStatus.PUBLISHED);
        Event event = eventRepository.save(e);
        TicketTier t = new TicketTier(event, "General Admission", 2500,
                100);
        TicketTier ticket = ticketTierRepository.save(t);
        ticketTierRepository.save(ticket);

        UpdateTicketTierRequest update = new UpdateTicketTierRequest("New Name", null, null);

        TicketTierResponse result = ticketTierService.updateTicketTier(ticket.getId(), update);

        assertThat(result.capacity()).isEqualTo(100);
        assertThat(result.tierName()).isEqualTo("New Name");
    }

}
