package com.savemyseat.event;


import com.savemyseat.auth.CurrentUserProvider;
import com.savemyseat.event.dto.CreateEventRequest;
import com.savemyseat.event.dto.EventResponse;
import com.savemyseat.event.dto.UpdateEventRequest;
import com.savemyseat.venue.Venue;
import com.savemyseat.venue.VenueRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventService {
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final CurrentUserProvider currentUserProvider;
    private final MeterRegistry registry;

    @Transactional
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventResponse createEvent(CreateEventRequest dto){
        Timer.Sample sample = Timer.start(registry);
        try {
            Venue venue =
                    venueRepository.findById(dto.venueId()).orElseThrow(() -> new EntityNotFoundException("Venue not found: " + dto.venueId()));

            if (!Objects.equals(venue.getOrganizer().getId(),
                    currentUserProvider.getCurrentUser().getId())) {
                throw new EntityNotFoundException("Venue not found: " + dto.venueId());
            }

            Event event = new Event(
                    venue,
                    dto.name(),
                    dto.description(),
                    dto.startsAt(),
                    dto.endsAt(),
                    EventStatus.DRAFT
            );
            registry.counter("events.created");
            return toResponse(eventRepository.save(event));
        }finally{
            sample.stop(registry.timer("event.creation.time"));
        }
    }

    public Page<EventResponse> listEvents(Long venueId,
                                          EventStatus status,
                                          Pageable pageable){
        Page<Event> events;
        if(venueId != null && status != null){
            events = eventRepository.findByVenueIdAndStatus(venueId, status,
                    pageable);
        }else if(venueId != null){
            events = eventRepository.findByVenueId(venueId, pageable);
        }else if (status != null){
            events = eventRepository.findByStatus(status, pageable);
        }else{
            events = eventRepository.findAll(pageable);
        }
        return events.map(this::toResponse);
    }

    public EventResponse getEventById(Long eventId){
        return eventRepository.findById(eventId).map(this::toResponse).orElseThrow(() -> new EntityNotFoundException("Event Not found: " + eventId));
    }

    @Transactional
    @PreAuthorize("hasRole('ORGANIZER')")
    public void deleteEventById(Long eventId){
        Event event = requireOwnedEvent(eventId);
        eventRepository.delete(event);
    }

    @Transactional
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventResponse updateEvent(Long eventId, UpdateEventRequest dto){
        Event event = requireOwnedEvent(eventId);

        if(dto.name() != null) event.setName(dto.name());
        if(dto.description() != null) event.setDescription(dto.description());
        if(dto.startsAt() != null) event.setStartsAt(dto.startsAt());
        if(dto.endsAt() != null) event.setEndsAt(dto.endsAt());
        if(dto.status() != null) {
            event.setStatus(dto.status());
            if(event.getStatus() == EventStatus.PUBLISHED){
                registry.counter("event.published").increment();
            }
        }

        return toResponse(eventRepository.save(event));
    }

    private Event requireOwnedEvent(Long eventId){
        Event event =
                eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException("Event not found: " + eventId));

        Long ownerId = event.getVenue().getOrganizer().getId();
        Long currentUserId = currentUserProvider.getCurrentUser().getId();

        if(!Objects.equals(ownerId, currentUserId)){
            throw new EntityNotFoundException("Event not found: " + eventId);
        }

        return event;
    }

    private EventResponse toResponse(Event event){
        return new EventResponse(
                event.getId(),
                event.getVenue().getId(),
                event.getName(),
                event.getDescription(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }


}
