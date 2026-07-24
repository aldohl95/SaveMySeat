package com.savemyseat.tickettier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketTierRepository extends JpaRepository<TicketTier, Long> {

    Page<TicketTier> findByEventId(Long eventId, Pageable pageable);

}
