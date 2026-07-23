package com.savemyseat.repository;

import com.savemyseat.entity.Event;
import com.savemyseat.entity.TicketTier;
import com.savemyseat.entity.User;
import com.savemyseat.entity.Venue;
import com.savemyseat.enums.EventStatus;
import com.savemyseat.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TicketTierRepositoryTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired UserRepository userRepository;
    @Autowired VenueRepository venueRepository;
    @Autowired EventRepository eventRepository;
    @Autowired TicketTierRepository ticketTierRepository;

    @Test
    void savesAndReadsBackTicketTier() {
        User u = new User("tasi", "mathias", "tasimathias@yexample.com",
                "hash", Role.ORGANIZER);
        User organizer = userRepository.save(u);
        Venue v = new Venue(organizer, "bar","Description", "shadowbrook",
                "Oak" +
                " harbor",
                "Wa", "98277");
        Venue venue = venueRepository.save(v);
        Event e = new Event(venue, "barhop", "hop some bars",
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                EventStatus.DRAFT);
        Event event = eventRepository.save(e);
        TicketTier t = new TicketTier(event, "General Admission", 1000, 100);
        TicketTier saved = ticketTierRepository.save(t);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        TicketTier fetched =
                ticketTierRepository.findById(saved.getId()).orElseThrow();

        assertThat(fetched.getId()).isEqualTo(saved.getId());
        assertThat(fetched.getTierName()).isEqualTo(saved.getTierName());

    }

    @Test
    void findByEventId (){
        User organizer = userRepository.save(new User("stella", "Mathias",
                "stellamathias@example.com", "hash", Role.ORGANIZER));
        Venue venue = venueRepository.save(new Venue(organizer, "barh",
                "Description",
                "angela", "oak harbor", "Washington", "98277"));
        Event events = eventRepository.save(new Event(venue, "barhopping",
                "hop some bars",OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                EventStatus.DRAFT ));
        TicketTier tiers = ticketTierRepository.save(new TicketTier(events,
                "General Admission", 1000, 100));

        List<TicketTier> found =
                ticketTierRepository.findByEventId(events.getId());

        assertThat(found).hasSize(1);

    }

    @Test
    void rejectsOversellTier(){
        User organizer = userRepository.save(
                new User("Sam", "Green", "oversell@example.com", "hash", Role.ORGANIZER));
        Venue venue = venueRepository.save(
                new Venue(organizer, "Test Venue", "Description", "1 Main",
                        "Seattle", "WA", "98101"));
        Event event = eventRepository.save(new Event(venue, "Test Event", "desc",
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                EventStatus.DRAFT));

        TicketTier tier = new TicketTier(event, "VIP", 5000L, 100);
        tier.setReserved(60);
        tier.setSold(50);

        assertThatThrownBy(() -> ticketTierRepository.saveAndFlush(tier))
                .isInstanceOf(DataIntegrityViolationException.class);

    }

}
