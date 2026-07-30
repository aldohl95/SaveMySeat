package com.savemyseat.user.dto;

import com.savemyseat.user.Role;

import java.time.OffsetDateTime;

public record UserResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    Role role,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
