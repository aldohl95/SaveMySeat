package com.savemyseat.event;

import com.savemyseat.event.dto.CreateEventRequest;
import com.savemyseat.event.dto.EventResponse;
import com.savemyseat.event.dto.UpdateEventRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Events", description = "Event management endpoints")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "Create a hold")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event created " +
                    "successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest dto){
        EventResponse created = eventService.createEvent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "List all events")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Events List " +
                    "successful")
    })
    @GetMapping
    public ResponseEntity<Page<EventResponse>> listEvents(@RequestParam(required = false) Long venueId,
                                                          @RequestParam(required =
                                                            false) EventStatus status,
                                                          Pageable pageable){
        return ResponseEntity.ok(eventService.listEvents(venueId,status,
                pageable));
    }

    @Operation(summary = "Find event by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event found"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable("id") Long id){
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @Operation(summary = "Update event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event Updated " +
                    "successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable("id") Long id, @Valid @RequestBody UpdateEventRequest dto){
        EventResponse updated = eventService.updateEvent(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event deleted " +
                    "Successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") Long id){
        eventService.deleteEventById(id);
        return ResponseEntity.noContent().build();
    }

}
