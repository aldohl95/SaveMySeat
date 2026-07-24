package com.savemyseat.venue;

import com.savemyseat.user.User;
import com.savemyseat.user.UserRepository;
import com.savemyseat.venue.dto.CreateVenueRequest;
import com.savemyseat.venue.dto.UpdateVenueRequest;
import com.savemyseat.venue.dto.VenueResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VenueService {

    private static final String STUB_ORGANIZER_EMAIL = "dev@savemyseat.local";

    private final VenueRepository venueRepository;
    private final UserRepository userRepository;

    @Transactional
    public VenueResponse createVenue(CreateVenueRequest dto){
        //TODO(week-3): replace with security auth
        User organizer =
                userRepository.findByEmail(STUB_ORGANIZER_EMAIL).orElseThrow(()
                        -> new IllegalStateException(
                                "stub organizer missing -check v3 migration ran"));

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
    public void deleteVenueById(Long venueId){
        if(!venueRepository.existsById(venueId)){
            throw new EntityNotFoundException("Venue not found: " + venueId);
        }
        venueRepository.deleteById(venueId);
    }

    @Transactional
    public VenueResponse updateVenue(Long venueId, UpdateVenueRequest dto){
        Venue venue =
                venueRepository.findById(venueId).orElseThrow(() -> new EntityNotFoundException("Venue Not found: " + venueId));

        if (dto.name() != null) venue.setName(dto.name());
        if(dto.description() != null) venue.setDescription(dto.description());
        if(dto.streetName() != null) venue.setStreetName(dto.streetName());
        if(dto.city() != null) venue.setCity(dto.city());
        if(dto.state() != null) venue.setState(dto.state());
        if(dto.zip() != null) venue.setZip(dto.zip());

        return toResponse(venueRepository.save(venue));
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
