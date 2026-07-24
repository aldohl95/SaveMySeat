package com.savemyseat.tickettier;

import com.savemyseat.event.Event;
import com.savemyseat.event.EventRepository;
import com.savemyseat.tickettier.dto.CreateTicketTierRequest;
import com.savemyseat.tickettier.dto.TicketTierResponse;
import com.savemyseat.tickettier.dto.UpdateTicketTierRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketTierService {

    private final EventRepository eventRepository;
    private final TicketTierRepository ticketTierRepository;

    @Transactional
    public TicketTierResponse createTicketTier(CreateTicketTierRequest dto){
        Event event =
                eventRepository.findById(dto.eventId()).orElseThrow(() -> new EntityNotFoundException("Event not found: " + dto.eventId()));
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

    @Transactional
    public void deleteTicketTierById(Long ticketTierId){
        if(!ticketTierRepository.existsById(ticketTierId)){
            throw new EntityNotFoundException("Ticket Tier not found: " + ticketTierId);
        }
        ticketTierRepository.deleteById(ticketTierId);
    }

    @Transactional
    public TicketTierResponse updateTicketTier(Long ticketTierId,
                                               UpdateTicketTierRequest dto){
        TicketTier ticketTier =
                ticketTierRepository.findById(ticketTierId).orElseThrow(() -> new EntityNotFoundException("Ticket Tier not found: " + ticketTierId));

        if(dto.tierName() != null) ticketTier.setTierName(dto.tierName());
        if(dto.priceCents() != null) ticketTier.setPriceCents(dto.priceCents());
        if(dto.capacity() != null) {
            if (dto.capacity() < ticketTier.getReserved() + ticketTier.getSold()){
                throw new IllegalArgumentException("Cannot reduce capacity " +
                        "below" + (ticketTier.getReserved() + ticketTier.getSold()) + " tickets already reaserved or sold");
            }
        }

        return toResponse(ticketTierRepository.save(ticketTier));
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
