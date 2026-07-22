package com.savemyseat.repository;

import com.savemyseat.entity.User;
import com.savemyseat.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VenueRepository extends JpaRepository <Venue, Long> {

  List<Venue> findByOrganizer(User organizer);

}
