package com.savemyseat.tickettier.dto;


import jakarta.validation.constraints.*;

public record CreateTicketTierRequest(

        @NotNull
        Long eventId,

        @NotBlank
        @Size(max = 100)
        String tierName,

        @NotNull
        @PositiveOrZero
        Long priceCents,

        @Positive
        int capacity
) {
}
