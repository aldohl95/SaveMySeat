package com.savemyseat.venue;


import com.savemyseat.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Table(name = "venues")
@EntityListeners(AuditingEntityListener.class)
public class Venue {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
          "venues_id_seq")
  @SequenceGenerator(name = "venues_id_seq", sequenceName = "venues_id_seq",
          allocationSize = 50)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organizer_id", nullable = false)
  private User organizer;

  @Column(nullable = false)
  private String name;
  @Column(nullable = true)
  private String description;
  @Column(name = "street_name", nullable = false)
  private String streetName;
  @Column(nullable = false)
  private String city;
  @Column(nullable = false)
  private String state;
  @Column(nullable = false)
  private String zip;


  @Column(name = "created_at")
  @CreatedDate
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  @LastModifiedDate
  private OffsetDateTime updatedAt;

  public Venue(User organizer, String name, String description,
               String streetName,
               String city,
               String state, String zip) {
    this.organizer = organizer;
    this.name = name;
    this.description = description;
    this.streetName = streetName;
    this.city = city;
    this.state = state;
    this.zip = zip;
  }

  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(!(o instanceof Venue)) return false;

    return id != null && id.equals(((Venue) o).id);
  }

  @Override
  public int hashCode(){return getClass().hashCode();}

  @Override
  public String toString(){
    return "Venue{" +
            "Id=" + id +
            "Organizer Id=" + (organizer != null ? organizer.getId() : null) +
            "Name=" + name +
            "Street Name=" + streetName +
            "City=" + city +
            "State" + state +
            "Zip" + zip;
  }

}
