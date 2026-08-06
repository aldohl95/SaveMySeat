package com.savemyseat.venue;


import com.savemyseat.venue.dto.CreateVenueRequest;
import com.savemyseat.venue.dto.UpdateVenueRequest;
import com.savemyseat.venue.dto.VenueResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
@Tag(name = "Venue", description = "Venue management endpoints")
public class VenueController {

    private final VenueService venueService;

    @Operation(summary = "Create a venue")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venue " +
                    "successfully created")
    })
    @PostMapping
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody CreateVenueRequest dto) {
        VenueResponse created = venueService.createVenue(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "List all venues")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "All venues " +
                    "listed")
    })
    @GetMapping
    public ResponseEntity<Page<VenueResponse>> listVenues(Pageable pageable) {
        return ResponseEntity.ok(venueService.listVenues(pageable));
    }

    @Operation(summary = "Find venue by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venue found " +
                    "successfully"),
            @ApiResponse(responseCode = "404", description = "Venue not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> getVenueById(@PathVariable("id") Long id){
        return ResponseEntity.ok(venueService.getVenueById(id));
    }

    @Operation(summary = "Update venue")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venue updated " +
                    "successfully"),
            @ApiResponse(responseCode = "404", description = "Venue not found")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<VenueResponse> updateVenue(@PathVariable("id") Long id,@Valid @RequestBody UpdateVenueRequest dto){
        VenueResponse updated = venueService.updateVenue(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete venue by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venue " +
                    "successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Venue not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable("id") Long id){
        venueService.deleteVenueById(id);
        return ResponseEntity.noContent().build();
    }

}
