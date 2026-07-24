package com.savemyseat.event;

import com.savemyseat.venue.Venue;
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
@Table(name = "events")
@EntityListeners(AuditingEntityListener.class)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
            "events_id_seq")
    @SequenceGenerator(name = "events_id_seq", sequenceName = "events_id_seq",
    allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name= "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    @Column(name = "starts_at",nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private OffsetDateTime endsAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(name = "created_at")
    @CreatedDate
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private OffsetDateTime updatedAt;

    public Event(Venue venue, String name, String description,
                 OffsetDateTime startsAt, OffsetDateTime endsAt,
                 EventStatus status) {
        this.venue = venue;
        this.name = name;
        this.description = description;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = status;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof Event)) return false;

        return id != null && id.equals(((Event)o).id);
    }

    @Override
    public int hashCode(){return getClass().hashCode();}

    @Override
    public String toString(){
        return "Event{" +
                "Id=" + id +
                ", Venue Id=" + (venue != null ? venue.getId() : null) +
                ", Name=" + name +
                ", description=" + description +
                ", Starts At=" + startsAt +
                ", Ends At=" + endsAt +
                ", Status=" + status +
                "}";
    }

}
