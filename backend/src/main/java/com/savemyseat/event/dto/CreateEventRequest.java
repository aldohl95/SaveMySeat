package com.savemyseat.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateEventRequest(
        @NotNull
        Long venueId,

        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        @NotNull
        OffsetDateTime startsAt,

        @NotNull
        OffsetDateTime endsAt

) {
}
