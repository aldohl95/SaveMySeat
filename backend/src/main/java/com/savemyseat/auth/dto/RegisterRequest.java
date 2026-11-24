package com.savemyseat.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotBlank
        @Email
        String email,

        @NotBlank
        // BCrypt only uses the first 72 bytes. This counts characters, so a
        // multibyte password can still exceed 72 bytes; the encoder rejects
        // those and GlobalExceptionHandler maps that to 400.
        @Size(min = 8, max = 72,
                message = "Password must be between 8 and 72 characters")
        String password
) {
}
