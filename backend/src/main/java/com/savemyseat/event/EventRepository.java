package com.savemyseat.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByVenueId(Long venueId, Pageable pageable);

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    Page<Event> findByVenueIdAndStatus(Long venueId, EventStatus status,
                                       Pageable pageable);

}
