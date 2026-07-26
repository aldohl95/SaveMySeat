package com.savemyseat.tickettier;

import com.savemyseat.tickettier.dto.CreateTicketTierRequest;
import com.savemyseat.tickettier.dto.TicketTierResponse;
import com.savemyseat.tickettier.dto.UpdateTicketTierRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickettiers")
public class TicketTierController {

    private final TicketTierService ticketTierService;

    @PostMapping
    public ResponseEntity<TicketTierResponse> createTicketTier(@Valid @RequestBody CreateTicketTierRequest dto){
        TicketTierResponse created = ticketTierService.createTicketTier(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<TicketTierResponse>> listTicketTiers(@RequestParam Long eventId,
                                                                    Pageable pageable){
        return ResponseEntity.ok(ticketTierService.listTicketTiers(eventId,
                pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketTierResponse> getTicketTierById(@PathVariable(
            "id") Long id){
        return ResponseEntity.ok(ticketTierService.getTicketTierById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TicketTierResponse> updateTicketTier(@PathVariable(
            "id") Long id,@Valid @RequestBody UpdateTicketTierRequest dto){
        TicketTierResponse updated = ticketTierService.updateTicketTier(id,
                dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicketTier(@PathVariable("id") Long id){
        ticketTierService.deleteTicketTierById(id);
        return ResponseEntity.noContent().build();
    }

}
