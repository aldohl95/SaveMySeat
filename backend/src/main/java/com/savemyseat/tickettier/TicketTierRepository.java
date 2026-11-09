package com.savemyseat.tickettier;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketTierRepository extends JpaRepository<TicketTier, Long> {

    Page<TicketTier> findByEventId(Long eventId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketTier t WHERE t.id = :id")
    Optional<TicketTier> findByIdWithLock(@Param("id") Long id);

}
