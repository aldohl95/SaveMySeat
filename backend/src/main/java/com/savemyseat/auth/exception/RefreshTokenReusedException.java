package com.savemyseat.auth.exception;

public class RefreshTokenReusedException extends RuntimeException {
    public RefreshTokenReusedException(String message) {
        super(message);
    }
}
