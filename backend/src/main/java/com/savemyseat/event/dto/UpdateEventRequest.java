package com.savemyseat.event.dto;

import com.savemyseat.event.EventStatus;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record UpdateEventRequest(
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        OffsetDateTime startsAt,

        OffsetDateTime endsAt,

        EventStatus status
) {
}
