package com.savemyseat.service;

import com.savemyseat.event.Event;
import com.savemyseat.event.EventRepository;
import com.savemyseat.event.EventStatus;
import com.savemyseat.hold.HoldRepository;
import com.savemyseat.hold.HoldService;
import com.savemyseat.hold.InsufficientCapacityException;
import com.savemyseat.hold.dto.CreateHoldRequest;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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
    @Autowired
    HoldService holdService;
    @Autowired
    HoldRepository holdRepository;

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

    @Test
    void concurrentHoldsRespectCapacity() throws Exception {

        // Arrange
        User organizer = userRepository.save(
                new User(
                        "calvin",
                        "pana",
                        "calvin@example.com",
                        "hash",
                        Role.ORGANIZER));

        User attendee = userRepository.save(
                new User(
                        "alska",
                        "ziegler",
                        "alaska@example.com",
                        "hash",
                        Role.ATTENDEE));

        Venue venue = venueRepository.save(
                new Venue(
                        organizer,
                        "AlaskaHouse",
                        "place to hang with alaska",
                        "shadowbrook",
                        "oak harbor",
                        "Washington",
                        "98277"));

        Event event = eventRepository.save(
                new Event(
                        venue,
                        "Hangout",
                        "Hang out with Alaska",
                        OffsetDateTime.now(ZoneOffset.UTC).plusHours(1),
                        OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                        EventStatus.PUBLISHED));

        TicketTier tier = ticketTierRepository.save(
                new TicketTier(event, "GA", 1000L, 10));

        int threadCount = 50;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finishGate = new CountDownLatch(threadCount);

        List<Future<Boolean>> futures = new ArrayList<>();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        attendee.getId().toString(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ATTENDEE")));

        // Submit all tasks
        for (int i = 0; i < threadCount; i++) {

            futures.add(executor.submit(() -> {

                try {

                    SecurityContextHolder.getContext().setAuthentication(auth);

                    // Wait until every thread is ready
                    startGate.await();

                    holdService.createHold(
                            new CreateHoldRequest(tier.getId(), 1));

                    return true;

                } catch (InsufficientCapacityException e) {

                    return false;

                } finally {

                    SecurityContextHolder.clearContext();
                    finishGate.countDown();
                }
            }));
        }

        // Release all threads simultaneously
        startGate.countDown();

        // Wait for every thread to finish
        assertThat(finishGate.await(30, TimeUnit.SECONDS)).isTrue();

        long successes = 0;

        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successes++;
            }
        }

        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        // Assert exactly 10 succeeded
        assertThat(successes).isEqualTo(10);

        TicketTier refreshed =
                ticketTierRepository.findById(tier.getId()).orElseThrow();

        assertThat(refreshed.getReserved()).isEqualTo(10);

        // Optional but recommended
        assertThat(holdRepository.count()).isEqualTo(10);
    }

}
