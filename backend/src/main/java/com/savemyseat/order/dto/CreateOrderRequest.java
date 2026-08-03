package com.savemyseat.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.aspectj.weaver.ast.Not;

public record CreateOrderRequest(

        @NotNull
        Long holdId
) {
}
