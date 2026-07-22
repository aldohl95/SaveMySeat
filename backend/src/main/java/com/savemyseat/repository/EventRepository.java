package com.savemyseat.repository;

import com.savemyseat.entity.Event;
import com.savemyseat.entity.Venue;
import com.savemyseat.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByVenueId(Long venueId);

    List<Event> findByStatus(EventStatus status);

}
