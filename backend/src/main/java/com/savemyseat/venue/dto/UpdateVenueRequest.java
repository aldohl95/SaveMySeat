package com.savemyseat.venue.dto;

import jakarta.validation.constraints.Size;
//These will be patch style optional fields
public record UpdateVenueRequest(
        @Size(max = 100)
        String name,
        @Size(max = 500)
        String description,
        @Size(max = 100)
        String streetName,
        @Size(max = 100)
        String city,
        @Size(max = 100)
        String state,
        @Size(max = 20)
        String zip
) { }
