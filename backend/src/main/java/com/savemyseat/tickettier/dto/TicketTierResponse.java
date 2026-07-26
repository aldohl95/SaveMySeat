package com.savemyseat.tickettier.dto;

import java.time.OffsetDateTime;

public record TicketTierResponse(
        Long id,
        Long eventId,
        String tierName,
        Long priceCents,
        int capacity,
        int reserved,
        int sold,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) { }
