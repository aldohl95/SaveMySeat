package com.savemyseat.tickettier;

import com.savemyseat.tickettier.dto.CreateTicketTierRequest;
import com.savemyseat.tickettier.dto.TicketTierResponse;
import com.savemyseat.tickettier.dto.UpdateTicketTierRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "TicketTier", description = "TicketTier management endpoints")
public class TicketTierController {

    private final TicketTierService ticketTierService;

    @Operation(summary = "Create a Ticket Tier")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket tier " +
                    "created successfully"),
            @ApiResponse(responseCode = "404", description = "Event not" +
                    " found")
    })
    @PostMapping
    public ResponseEntity<TicketTierResponse> createTicketTier(@Valid @RequestBody CreateTicketTierRequest dto){
        TicketTierResponse created = ticketTierService.createTicketTier(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "List all ticketTiers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets listed " +
                    "successfully")
    })
    @GetMapping
    public ResponseEntity<Page<TicketTierResponse>> listTicketTiers(@RequestParam Long eventId,
                                                                    Pageable pageable){
        return ResponseEntity.ok(ticketTierService.listTicketTiers(eventId,
                pageable));
    }

    @Operation(summary = "Find ticket tier by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket tier " +
                    "found successfully"),
            @ApiResponse(responseCode = "404", description = "Ticket tier not" +
                    " found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketTierResponse> getTicketTierById(@PathVariable(
            "id") Long id){
        return ResponseEntity.ok(ticketTierService.getTicketTierById(id));
    }

    @Operation(summary = "Update ticket tier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket tier " +
                    "updated successfully"),
            @ApiResponse(responseCode = "404", description = "Ticket tier not" +
                    " found"),
            @ApiResponse(responseCode = "400", description = "Capacity cannot" +
                    " be reduced below zero")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<TicketTierResponse> updateTicketTier(@PathVariable(
            "id") Long id,@Valid @RequestBody UpdateTicketTierRequest dto){
        TicketTierResponse updated = ticketTierService.updateTicketTier(id,
                dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete ticket tier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket tier " +
                    "deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Ticket tier not" +
                    " found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicketTier(@PathVariable("id") Long id){
        ticketTierService.deleteTicketTierById(id);
        return ResponseEntity.noContent().build();
    }

}
