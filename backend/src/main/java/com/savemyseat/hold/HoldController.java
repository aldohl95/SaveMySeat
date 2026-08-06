package com.savemyseat.hold;

import com.savemyseat.hold.dto.CreateHoldRequest;
import com.savemyseat.hold.dto.HoldResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/holds")
@Tag(name = "Holds", description = "Hold management endpoints")
public class HoldController {

    private final HoldService holdService;

    @Operation(summary = "Creates a hold")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hold created " +
                    "successfully"),
            @ApiResponse(responseCode = "404", description = "Tier not found"),
            @ApiResponse(responseCode = "409", description = "Not enough " +
                    "tickets")
    })
    @PostMapping
    public ResponseEntity<HoldResponse> createHold(@Valid @RequestBody CreateHoldRequest dto){
        HoldResponse created = holdService.createHold(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Find a hold")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hold found " +
                    "successfully"),
            @ApiResponse(responseCode = "404", description = "Hold not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<HoldResponse> getHoldById(@PathVariable("id") Long id){
        return ResponseEntity.ok(holdService.getHoldById(id));
    }

    @Operation(summary = "release a hold")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hold deleted " +
                    "successfully"),
            @ApiResponse(responseCode = "404", description = "Hold not found"),
            @ApiResponse(responseCode = "409", description = "Hold status " +
                    "incorrect")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<HoldResponse> releaseHold(@PathVariable("id") Long id){
        return ResponseEntity.ok(holdService.releaseHold(id));
    }


}
