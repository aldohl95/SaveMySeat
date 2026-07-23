package com.savemyseat.repository;

import com.savemyseat.event.Event;
import com.savemyseat.event.EventRepository;
import com.savemyseat.user.User;
import com.savemyseat.user.UserRepository;
import com.savemyseat.venue.Venue;
import com.savemyseat.event.EventStatus;
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
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EventRepositoryTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine");

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

    @Test
    void savesAndReadsBackEvent(){
        User u = new User("Tevita", "james", "tj@example.com", "hash",
                Role.ORGANIZER);
        User organizer = userRepository.save(u);
        Venue v = new Venue(organizer, "BarName","d", "shadowbrrok",
                "Oakhardbor",
                "Washington", "98277");
        Venue tVenue = venueRepository.save(v);
        Event e = new Event(tVenue, "DateNight", "Find a date",
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                EventStatus.DRAFT);
        Event saved = eventRepository.save(e);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Event fetched =
                eventRepository.findById(saved.getId()).orElseThrow();

        assertThat(fetched.getName()).isEqualTo(saved.getName());
        assertThat(fetched.getId()).isEqualTo(saved.getId());

    }

    @Test
    void findAllEventsByVenue(){

        User organizer = userRepository.save(new User("Brene", "delarosa"
                ,"ab@example.com", "hash", Role.ORGANIZER));
        Venue venue = venueRepository.save(new Venue(organizer, "Home",
                "description",
                "Shadowbrook", "Oak Harbor"
                , "Washington", "98277"));
        Event event1 = eventRepository.save(new Event(venue, "DateNight",
                "find a date",
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                EventStatus.DRAFT));
        Event event2 = eventRepository.save(new Event(venue, "SinglesNight",
                "find some singles",OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                EventStatus.DRAFT ));
        List<Event> found = eventRepository.findByVenueId(venue.getId());

        assertThat(found).hasSize(2);


    }

    @Test
    void findAllEventsByStatus(){
        User organizer = userRepository.save(new User("tim", "delarosa"
                ,"tim@example.com", "hash", Role.ORGANIZER));
        Venue venue = venueRepository.save(new Venue(organizer, "bar2",
                "descritpion",
                "Shadowbrook", "Oak Harbor"
                , "Washington", "98277"));
        Event draftEvent = eventRepository.save(new Event(venue, "DateNight",
                "find a date",
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                EventStatus.DRAFT));
        Event publishedEvent = eventRepository.save(new Event(venue, "SinglesNight",
                "find some singles",
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                EventStatus.PUBLISHED));

        List<Event> drafts = eventRepository.findByStatus(EventStatus.DRAFT);

        assertThat(drafts).extracting(Event::getId).contains(draftEvent.getId());
        assertThat(drafts).extracting(Event::getId).doesNotContain(publishedEvent.getId());
    }

}
