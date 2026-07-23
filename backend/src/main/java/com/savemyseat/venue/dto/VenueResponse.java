package com.savemyseat.venue.dto;

import java.time.OffsetDateTime;

public record VenueResponse(
    Long id,
    Long organizerId,
    String name,
    String description,
    String streetName,
    String city,
    String state,
    String zip,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) { }
