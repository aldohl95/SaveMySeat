package com.savemyseat.hold;


import com.savemyseat.tickettier.TicketTier;
import com.savemyseat.tickettier.TicketTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldSweeperService {

    private final HoldRepository holdRepository;
    private final TicketTierRepository ticketTierRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireHolds(){

        List<Hold> expiredHolds =
                holdRepository.findByStatusAndExpiresAtBefore(HoldStatus.ACTIVE, OffsetDateTime.now(ZoneOffset.UTC));

        for(Hold hold : expiredHolds){

            TicketTier tier = hold.getTicketTier();

            tier.setReserved(tier.getReserved() - hold.getQuantity());

            hold.setStatus(HoldStatus.EXPIRED);

            ticketTierRepository.save(tier);
            holdRepository.save(hold);

        }

    }


}
