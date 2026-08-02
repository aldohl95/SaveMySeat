package com.savemyseat.hold;

import com.savemyseat.auth.CurrentUserProvider;
import com.savemyseat.hold.dto.CreateHoldRequest;
import com.savemyseat.hold.dto.HoldResponse;
import com.savemyseat.tickettier.TicketTier;
import com.savemyseat.tickettier.TicketTierRepository;
import com.savemyseat.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HoldService {

    private final HoldRepository holdRepository;
    private final TicketTierRepository ticketTierRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public HoldResponse createHold(CreateHoldRequest dto){
        User user = currentUserProvider.getCurrentUser();

        TicketTier tier =
                ticketTierRepository.findByIdWithLock(dto.tierId()).orElseThrow(() -> new EntityNotFoundException("Tier not found: " + dto.tierId()));
        int available =
                tier.getCapacity() - tier.getSold() - tier.getReserved();
        if (dto.quantity() > available){
            throw new InsufficientCapacityException("Insufficient Tickets " +
                    "available: request " + dto.quantity() + ", available " + available);
        }

        tier.setReserved(tier.getReserved() + dto.quantity());

        Hold hold = new Hold(user, tier, dto.quantity(),
                OffsetDateTime.now(ZoneOffset.UTC).plus(10, ChronoUnit.MINUTES));
        Hold saved = holdRepository.saveAndFlush(hold);

        return toResponse(saved);

    }
    @Transactional
    public HoldResponse releaseHold(Long holdId){

        Hold hold =
                holdRepository.findById(holdId).orElseThrow(() -> new EntityNotFoundException(
                        "hold not found: " + holdId));
        User user = currentUserProvider.getCurrentUser();
        if(!Objects.equals(user.getId(), hold.getUser().getId())){
            throw new EntityNotFoundException("Hold not found: " + hold.getId());
        }

        if(hold.getStatus() != HoldStatus.ACTIVE){
            throw new IllegalStateException("Hold cannot be released from " +
                    "status: " + hold.getStatus());
        }

        TicketTier tier =
                ticketTierRepository.findByIdWithLock(hold.getTicketTier().getId()).orElseThrow(() -> new EntityNotFoundException("Tier not found: " + hold.getTicketTier().getId()));
        tier.setReserved(tier.getReserved() - hold.getQuantity());
        hold.setStatus(HoldStatus.RELEASED);

        Hold saved = holdRepository.saveAndFlush(hold);

        return toResponse(saved);

    }

    public HoldResponse getHoldById(Long holdId){
        Hold hold =
                holdRepository.findById(holdId).orElseThrow(() -> new EntityNotFoundException("Hold not found: " + holdId));

        User currentUser = currentUserProvider.getCurrentUser();

        if(!Objects.equals(currentUser.getId(), hold.getUser().getId())){
            throw new EntityNotFoundException("Hold not found: " + holdId);
        }

        return toResponse(hold);

    }
    //Internal only will be called by orderservice after payment succeeds no
    // Http exposure
    @Transactional
    public HoldResponse convertHold (Long holdId){
        Hold hold =
                holdRepository.findById(holdId).orElseThrow(() -> new EntityNotFoundException("Hold not found: " + holdId));
        if(hold.getStatus() != HoldStatus.ACTIVE){
            throw new IllegalStateException("Hold cannot not be converted " +
                    "from status: " + hold.getStatus());
        }

        if(OffsetDateTime.now(ZoneOffset.UTC).isAfter(hold.getExpiresAt())){
            throw new IllegalStateException("Hold has expired");
        }

        TicketTier tier =
                ticketTierRepository.findByIdWithLock(hold.getTicketTier().getId()).orElseThrow(() -> new EntityNotFoundException("Tier not found: " + hold.getTicketTier().getId()));
        tier.setReserved(tier.getReserved() - hold.getQuantity());
        tier.setSold(tier.getSold() + hold.getQuantity());
        hold.setStatus(HoldStatus.CONVERTED);
        Hold saved = holdRepository.saveAndFlush(hold);

        return toResponse(saved);

    }


    private HoldResponse toResponse(Hold hold){
        return new HoldResponse(
                hold.getId(),
                hold.getTicketTier().getId(),
                hold.getQuantity(),
                hold.getStatus(),
                hold.getExpiresAt(),
                hold.getCreatedAt(),
                hold.getUpdatedAt()
        );
    }

}
