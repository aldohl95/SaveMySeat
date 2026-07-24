package com.savemyseat.venue;


import com.savemyseat.venue.dto.CreateVenueRequest;
import com.savemyseat.venue.dto.UpdateVenueRequest;
import com.savemyseat.venue.dto.VenueResponse;
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
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody CreateVenueRequest dto) {
        VenueResponse created = venueService.createVenue(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<VenueResponse>> listVenues(Pageable pageable) {
        return ResponseEntity.ok(venueService.listVenues(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> getVenueById(@PathVariable("id") Long id){
        return ResponseEntity.ok(venueService.getVenueById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<VenueResponse> updateVenue(@PathVariable("id") Long id,@Valid @RequestBody UpdateVenueRequest dto){
        VenueResponse updated = venueService.updateVenue(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable("id") Long id){
        venueService.deleteVenueById(id);
        return ResponseEntity.noContent().build();
    }

}
