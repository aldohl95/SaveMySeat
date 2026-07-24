package com.savemyseat.event;

import com.savemyseat.event.dto.CreateEventRequest;
import com.savemyseat.event.dto.EventResponse;
import com.savemyseat.event.dto.UpdateEventRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest dto){
        EventResponse created = eventService.createEvent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<EventResponse>> listEvents(@RequestParam(required = false) Long venueId,
                                                          @RequestParam(required =
                                                            false) EventStatus status,
                                                          Pageable pageable){
        return ResponseEntity.ok(eventService.listEvents(venueId,status,
                pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable("id") Long id){
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable("id") Long id, @Valid @RequestBody UpdateEventRequest dto){
        EventResponse updated = eventService.updateEvent(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") Long id){
        eventService.deleteEventById(id);
        return ResponseEntity.noContent().build();
    }

}
