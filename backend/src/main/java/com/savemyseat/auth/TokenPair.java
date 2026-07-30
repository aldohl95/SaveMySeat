package com.savemyseat.auth;

public record TokenPair(String plainText, RefreshToken entity) {
}
