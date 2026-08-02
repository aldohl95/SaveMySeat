package com.savemyseat.hold.dto;

import com.savemyseat.hold.HoldStatus;

import java.time.OffsetDateTime;

public record HoldResponse(
        Long id,
        Long tierid,
        Integer quantity,
        HoldStatus status,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
