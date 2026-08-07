package com.savemyseat.hold;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface HoldRepository extends JpaRepository<Hold, Long> {

    @Query("SELECT h.id FROM Hold h WHERE h.status = :status AND h.expiresAt < :now")
    List<Long> findIdsByStatusAndExpiresAtBefore(
            @Param("status") HoldStatus status,
            @Param("now") OffsetDateTime now
    );

    @Modifying
    @Query("UPDATE Hold h SET h.status = 'EXPIRED' WHERE h.id = :id AND h.status = 'ACTIVE' AND h.expiresAt < :now")
    int markExpiredIfActive(@Param("id") Long id, @Param("now") OffsetDateTime now);

}
