package com.savemyseat.hold;


import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface HoldRepository extends JpaRepository<Hold, Long> {

    List<Hold> findByStatusAndExpiresAtBefore(
            HoldStatus status,
            OffsetDateTime now
    );

}
