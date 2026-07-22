package com.savemyseat.repository;

import com.savemyseat.entity.Event;
import com.savemyseat.entity.TicketTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketTierRepository extends JpaRepository<TicketTier, Long> {

    List<TicketTier> findByEventId(Long eventId);

}
