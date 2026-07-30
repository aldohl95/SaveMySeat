package com.savemyseat.auth;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("security.jwt")
public record JwtProperties(String secret, int expirationHours) {
}
