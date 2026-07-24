package com.savemyseat.tickettier.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateTicketTierRequest(
        @Size(max = 100)
        String tierName,

        @PositiveOrZero
        Long priceCents,

        @Positive
        Integer capacity
) {

}
