package com.savemyseat.order;

import com.savemyseat.hold.Hold;
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
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator =
            "orders_id_seq")
    @SequenceGenerator(name = "orders_id_seq", sequenceName =
            "orders_id_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hold_id", nullable = false)
    private Hold hold;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private TicketTier ticketTier;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "total_cents", nullable = false)
    private Long totalCents;

    @Column(name = "stripe_session_id")
    private String stripeSessionId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private OffsetDateTime updatedAt;

    public Order(User user, Hold hold, TicketTier ticketTier, int quantity, Long totalCents) {
        this.user = user;
        this.hold = hold;
        this.ticketTier = ticketTier;
        this.quantity = quantity;
        this.totalCents = totalCents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Order)) return false;
        return id != null && id.equals(((Order) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString(){
        return "{" +
                "Id: " + id +
                ", User Id: " + (user != null ? user.getId() : null) +
                ", Hold Id: " + (hold != null ? hold.getId() : null) +
                ", Tier Id: " + (ticketTier != null ? ticketTier.getId() :
                null) +
                ", Quantity: " + quantity +
                ", Total(Cents): " + totalCents +
                ", Stripe Session Id: " + stripeSessionId +
                ", Status: " + status +
                ", Created At: " + createdAt +
                ", Updated At: " + updatedAt +
                "}";
    }

}
