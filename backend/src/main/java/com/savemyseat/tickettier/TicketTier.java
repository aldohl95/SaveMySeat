package com.savemyseat.tickettier;

import com.savemyseat.event.Event;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ticket_tiers")
public class TicketTier {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator =
            "ticket_tiers_id_seq")
    @SequenceGenerator(name = "ticket_tiers_id_seq", sequenceName =
            "ticket_tiers_id_seq", allocationSize = 50)
    private Long id;

    @Column(name = "tier_name", nullable = false)
    private String tierName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "price_cents", nullable = false)
    private long priceCents;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int reserved = 0;

    @Column(nullable = false)
    private int sold = 0;

    @Column(name = "created_at")
    @CreatedDate
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private OffsetDateTime updatedAt;

    public TicketTier(Event event, String tierName,
                      long priceCents, int capacity) {
        this.tierName = tierName;
        this.event = event;
        this.priceCents = priceCents;
        this.capacity = capacity;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof TicketTier)) return false;
        return id != null && id.equals(((TicketTier)o).id);

    }

    @Override
    public int hashCode(){return getClass().hashCode();}

    @Override
    public String toString(){
        return "{" +
                "Id:" + id +
                ", Event:" + (event != null ? event.getId() : null) +
                ", Tier Name:" + tierName +
                ", Price(Cents):" + priceCents +
                ", Capacity:" + capacity +
                ", Reserved:" + reserved +
                ", Sold:" + sold +
                "}";
    }

}
