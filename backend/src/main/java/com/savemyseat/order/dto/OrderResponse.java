package com.savemyseat.order.dto;

import com.savemyseat.order.OrderStatus;

import java.time.OffsetDateTime;

public record OrderResponse(
        Long id,
        Long userId,
        Long holdId,
        Long tierId,
        Integer quantity,
        Long totalCents,
        String stripeSessionId,
        OrderStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
