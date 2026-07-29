package com.savemyseat.auth;


import com.savemyseat.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Table(name = "refresh_tokens")
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
            "refresh_tokens_id_seq")
    @SequenceGenerator(name = "refresh_tokens_id_seq", sequenceName = "refresh_tokens_id_seq",
            allocationSize = 50)
    private Long id;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_id")
    private RefreshToken replacedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    @Column(name = "created_at")
    @CreatedDate
    private OffsetDateTime createdAt;

    public RefreshToken(User user, String tokenHash, OffsetDateTime expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof RefreshToken)) return false;

        return id != null && id.equals(((RefreshToken)o).id);
    }

    @Override
    public int hashCode(){return getClass().hashCode();}

    @Override
    public String toString(){
        return "RefreshToken{" +
                " id: " + id +
                ", UserId: " + (user != null ? user.getId() : null) +
                ", Replaced By: " + (replacedBy != null ? replacedBy.getId()
                : null) +
                ", Expires At: " + expiresAt +
                ", Used At: " + usedAt +
                ", Created At: " + createdAt +
                "}";
    }

}
