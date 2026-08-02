package com.savemyseat.hold;

import com.savemyseat.hold.dto.CreateHoldRequest;
import com.savemyseat.hold.dto.HoldResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/holds")
public class HoldController {

    private final HoldService holdService;

    @PostMapping
    public ResponseEntity<HoldResponse> createHold(@Valid @RequestBody CreateHoldRequest dto){
        HoldResponse created = holdService.createHold(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HoldResponse> getHoldById(@PathVariable("id") Long id){
        return ResponseEntity.ok(holdService.getHoldById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HoldResponse> releaseHold(@PathVariable("id") Long id){
        return ResponseEntity.ok(holdService.releaseHold(id));
    }


}
