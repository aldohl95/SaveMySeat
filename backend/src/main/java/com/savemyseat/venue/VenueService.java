package com.savemyseat.venue;

import com.savemyseat.auth.CurrentUserProvider;
import com.savemyseat.auth.exception.InvalidCredentialsException;
import com.savemyseat.user.User;
import com.savemyseat.user.UserRepository;
import com.savemyseat.venue.dto.CreateVenueRequest;
import com.savemyseat.venue.dto.UpdateVenueRequest;
import com.savemyseat.venue.dto.VenueResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VenueService {


    private final CurrentUserProvider currentUserProvider;
    private final VenueRepository venueRepository;

    @Transactional
    @PreAuthorize("hasRole('ORGANIZER')")
    public VenueResponse createVenue(CreateVenueRequest dto){

        User organizer = currentUserProvider.getCurrentUser();

        Venue venue = new Venue(
                organizer,
                dto.name(),
                dto.description(),
                dto.streetName(),
                dto.city(),
                dto.state(),
                dto.zip()
        );


        return toResponse(venueRepository.save(venue));

    }

    public Page<VenueResponse> listVenues(Pageable pageable){
        return venueRepository.findAll(pageable).map(this::toResponse);
    }

    public VenueResponse getVenueById(Long venueId){
        return venueRepository.findById(venueId).map(this::toResponse).orElseThrow(()
                -> new EntityNotFoundException("Venue not found: " + venueId));

    }

    @Transactional
    @PreAuthorize("hasRole('ORGANIZER')")
    public void deleteVenueById(Long venueId){
        Venue venue = requireOwnedVenue(venueId);
        venueRepository.delete(venue);
    }

    @Transactional
    @PreAuthorize("hasRole('ORGANIZER')")
    public VenueResponse updateVenue(Long venueId, UpdateVenueRequest dto){
        Venue venue = requireOwnedVenue(venueId);

        if (dto.name() != null) venue.setName(dto.name());
        if(dto.description() != null) venue.setDescription(dto.description());
        if(dto.streetName() != null) venue.setStreetName(dto.streetName());
        if(dto.city() != null) venue.setCity(dto.city());
        if(dto.state() != null) venue.setState(dto.state());
        if(dto.zip() != null) venue.setZip(dto.zip());

        return toResponse(venueRepository.save(venue));
    }

    private Venue requireOwnedVenue(Long venueId){
        Venue venue =
                venueRepository.findById(venueId).orElseThrow(() -> new EntityNotFoundException("Venue not found: " + venueId));
        if(!Objects.equals(venue.getOrganizer().getId(),
                currentUserProvider.getCurrentUser().getId())){
            throw new EntityNotFoundException("Venue not found: " + venueId);
        }

        return venue;

    }


    private VenueResponse toResponse(Venue venue){
        return new VenueResponse(
                venue.getId(),
                venue.getOrganizer().getId(),
                venue.getName(),
                venue.getDescription(),
                venue.getStreetName(),
                venue.getCity(),
                venue.getState(),
                venue.getZip(),
                venue.getCreatedAt(),
                venue.getUpdatedAt()
        );
    }

}
