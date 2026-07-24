package com.savemyseat.event.dto;

import com.savemyseat.event.EventStatus;

import java.time.OffsetDateTime;

public record EventResponse(
        Long id,
        Long venueId,
        String name,
        String description,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        EventStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
