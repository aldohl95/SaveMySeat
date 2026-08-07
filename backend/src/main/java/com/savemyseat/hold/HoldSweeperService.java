package com.savemyseat.hold;


import com.savemyseat.tickettier.TicketTier;
import com.savemyseat.tickettier.TicketTierRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldSweeperService {

    private final HoldRepository holdRepository;
    private final TicketTierRepository ticketTierRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireHolds() {
        List<Long> candidateIds = holdRepository.findIdsByStatusAndExpiresAtBefore(
                HoldStatus.ACTIVE, OffsetDateTime.now(ZoneOffset.UTC));

        for (Long holdId : candidateIds) {
            expireOne(holdId);
        }
    }

    public void expireOne(Long holdId) {
        int rowsUpdated = holdRepository.markExpiredIfActive(holdId, OffsetDateTime.now(ZoneOffset.UTC));
        if (rowsUpdated == 0) {
            return;
        }

        Hold hold =
                holdRepository.findById(holdId).orElseThrow(()-> new EntityNotFoundException("Hold not found: " + holdId));
        TicketTier tier = ticketTierRepository.findByIdWithLock(hold.getTicketTier().getId())
                .orElseThrow(() -> new EntityNotFoundException("Ticket tier " +
                        "not " +
                        "found: " + hold.getTicketTier().getId()));
        tier.setReserved(tier.getReserved() - hold.getQuantity());
    }


}
