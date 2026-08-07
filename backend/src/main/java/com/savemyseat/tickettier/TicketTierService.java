package com.savemyseat.tickettier;

import com.savemyseat.auth.CurrentUserProvider;
import com.savemyseat.event.Event;
import com.savemyseat.event.EventRepository;
import com.savemyseat.tickettier.dto.CreateTicketTierRequest;
import com.savemyseat.tickettier.dto.TicketTierResponse;
import com.savemyseat.tickettier.dto.UpdateTicketTierRequest;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketTierService {

    private final EventRepository eventRepository;
    private final TicketTierRepository ticketTierRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    @PreAuthorize("hasRole('ORGANIZER')")
    public TicketTierResponse createTicketTier(CreateTicketTierRequest dto){
        Event event =
                eventRepository.findById(dto.eventId()).orElseThrow(() -> new EntityNotFoundException("Event not found: " + dto.eventId()));

        if(!Objects.equals(event.getVenue().getOrganizer().getId(),
                currentUserProvider.getCurrentUser().getId())){
            throw new EntityNotFoundException("Event not found: " + dto.eventId());
        }

        TicketTier ticketTier = new TicketTier(
                event,
                dto.tierName(),
                dto.priceCents(),
                dto.capacity()
        );

        return toResponse(ticketTierRepository.save(ticketTier));
    }

    public TicketTierResponse getTicketTierById(Long ticketTierId){
        return ticketTierRepository.findById(ticketTierId).map(this::toResponse).orElseThrow(() -> new EntityNotFoundException("Ticket Tier not found: " + ticketTierId));
    }

    public Page<TicketTierResponse> listTicketTiers(Long eventId, Pageable pageable) {
        return ticketTierRepository.findByEventId(eventId, pageable).map(this::toResponse);
    }

    @Transactional
    @PreAuthorize("hasRole('ORGANIZER')")
    public void deleteTicketTierById(Long ticketTierId){
        TicketTier ticketTier = requireOwnedTicketTier(ticketTierId);
        ticketTierRepository.delete(ticketTier);
    }

    @Transactional
    @PreAuthorize("hasRole('ORGANIZER')")
    public TicketTierResponse updateTicketTier(Long ticketTierId,
                                               UpdateTicketTierRequest dto){
        TicketTier ticketTier = requireOwnedTicketTier(ticketTierId);

        if(dto.tierName() != null) ticketTier.setTierName(dto.tierName());
        if(dto.priceCents() != null) ticketTier.setPriceCents(dto.priceCents());
        if(dto.capacity() != null) {
            if (dto.capacity() < ticketTier.getReserved() + ticketTier.getSold()){
                throw new IllegalArgumentException("Cannot reduce capacity " +
                        "below" + (ticketTier.getReserved() + ticketTier.getSold()) + " tickets already reserved or sold");
            }
            ticketTier.setCapacity(dto.capacity());
        }

        return toResponse(ticketTierRepository.saveAndFlush(ticketTier));
    }

    private TicketTier requireOwnedTicketTier(Long ticketTierId){
        TicketTier ticketTier = ticketTierRepository.findById(ticketTierId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket Tier " +
                        "not found: " + ticketTierId));
        Long ownerId = ticketTier.getEvent().getVenue().getOrganizer().getId();
        Long currentUserId = currentUserProvider.getCurrentUser().getId();

        if(!Objects.equals(ownerId,currentUserId)){
            throw new EntityNotFoundException("TicketTier not found: " + ticketTierId);
        }

        return ticketTier;
    }

    private TicketTierResponse toResponse(TicketTier ticketTier){
        return new TicketTierResponse(
                ticketTier.getId(),
                ticketTier.getEvent().getId(),
                ticketTier.getTierName(),
                ticketTier.getPriceCents(),
                ticketTier.getCapacity(),
                ticketTier.getReserved(),
                ticketTier.getSold(),
                ticketTier.getCreatedAt(),
                ticketTier.getUpdatedAt()

        );
    }

}
