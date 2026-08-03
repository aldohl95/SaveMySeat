package com.savemyseat.order.dto;

import jakarta.validation.constraints.NotBlank;

public record MarkPaidRequest(@NotBlank String stripeSessionId) {
}
