package com.savemyseat.hold.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateHoldRequest(
    @NotNull
    Long tierId,

    @NotNull
    @Positive
    @Max(10)
    Integer quantity
) {
}
