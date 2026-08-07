package com.savemyseat.service;

import com.savemyseat.event.Event;
import com.savemyseat.event.EventRepository;
import com.savemyseat.event.EventStatus;
import com.savemyseat.hold.Hold;
import com.savemyseat.hold.HoldRepository;
import com.savemyseat.hold.HoldStatus;
import com.savemyseat.hold.HoldSweeperService;
import com.savemyseat.tickettier.TicketTier;
import com.savemyseat.tickettier.TicketTierRepository;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HoldSweeperServiceTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    HoldSweeperService holdSweeperService;

    @Autowired
    HoldRepository holdRepository;

    @Autowired
    TicketTierRepository ticketTierRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VenueRepository venueRepository;

    @Autowired
    EventRepository eventRepository;



    @Test
    void expiresExpiredHoldsAndReleasesReservedTickets() {

        User organizer = userRepository.save(
                new User("test", "user",
                        "test@example.com",
                        "hash",
                        Role.ORGANIZER)
        );

        Venue venue = venueRepository.save(
                new Venue(
                        organizer,
                        "Venue",
                        "desc",
                        "address",
                        "Oak Harbor",
                        "Washington",
                        "98277")
        );

        Event event = eventRepository.save(
                new Event(
                        venue,
                        "Concert",
                        "desc",
                        OffsetDateTime.now(ZoneOffset.UTC),
                        OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                        EventStatus.PUBLISHED)
        );

        TicketTier tier = new TicketTier(event, "GA", 100, 20);

        tier.setReserved(20);

        tier = ticketTierRepository.save(tier);

        Hold hold = new Hold(
                organizer,
                tier,
                5,
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5)
        );

        holdRepository.save(hold);


        holdSweeperService.expireHolds();


        Hold updatedHold =
                holdRepository.findById(hold.getId())
                        .orElseThrow();

        TicketTier updatedTier =
                ticketTierRepository.findById(tier.getId())
                        .orElseThrow();


        assertThat(updatedHold.getStatus())
                .isEqualTo(HoldStatus.EXPIRED);

        assertThat(updatedTier.getReserved())
                .isEqualTo(15);
    }

    @Test
    void doesNotExpireActiveHolds() {

        User organizer = userRepository.save(
                new User(
                        "active",
                        "user",
                        "active@example.com",
                        "hash",
                        Role.ORGANIZER)
        );

        Venue venue = venueRepository.save(
                new Venue(
                        organizer,
                        "Active Venue",
                        "desc",
                        "address",
                        "Oak Harbor",
                        "Washington",
                        "98277")
        );

        Event event = eventRepository.save(
                new Event(
                        venue,
                        "Active Concert",
                        "desc",
                        OffsetDateTime.now(ZoneOffset.UTC),
                        OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                        EventStatus.PUBLISHED)
        );

        TicketTier tier = new TicketTier(event, "GA", 100, 20);

        tier.setReserved(20);

        tier = ticketTierRepository.save(tier);


        Hold hold = new Hold(
                organizer,
                tier,
                5,
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(10)
        );

        holdRepository.save(hold);


        holdSweeperService.expireHolds();


        Hold updatedHold =
                holdRepository.findById(hold.getId())
                        .orElseThrow();

        TicketTier updatedTier =
                ticketTierRepository.findById(tier.getId())
                        .orElseThrow();


        assertThat(updatedHold.getStatus())
                .isEqualTo(HoldStatus.ACTIVE);

        assertThat(updatedTier.getReserved())
                .isEqualTo(20);
    }


}