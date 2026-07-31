package com.savemyseat.hold;

import com.savemyseat.tickettier.TicketTier;
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
@Table(name = "holds")
@EntityListeners(AuditingEntityListener.class)
public class Hold {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
            "holds_id_seq")
    @SequenceGenerator(name = "holds_id_seq", sequenceName = "holds_id_seq",
            allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private TicketTier ticketTier;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HoldStatus status = HoldStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private OffsetDateTime updatedAt;

    public Hold(User user, TicketTier ticketTier, int quantity, OffsetDateTime expiresAt) {
        this.user = user;
        this.ticketTier = ticketTier;
        this.quantity = quantity;
        this.expiresAt = expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Hold)) return false;
        return id != null && id.equals(((Hold) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Hold{" +
                "Id: " + id +
                ", User Id: " + (user != null ? user.getId() : null) +
                ", Ticket Id: " + (ticketTier != null ? ticketTier.getId() :
                null) +
                ", Quantity: " + quantity +
                ", Expires At: " + expiresAt +
                ", Status: " + status +
                "}";
    }


}
