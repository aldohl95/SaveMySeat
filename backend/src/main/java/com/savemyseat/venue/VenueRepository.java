package com.savemyseat.venue;

import com.savemyseat.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueRepository extends JpaRepository <Venue, Long> {

  List<Venue> findByOrganizer(User organizer);

}
