package br.com.maozinha.auth;

import br.com.maozinha.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@Table(name = "token_recuperacao")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "usuario_id",
            nullable = false
    )
    private User user;

    @Column(
            name = "token_hash",
            nullable = false,
            length = 64
    )
    private String tokenHash;

    @Column(
            name = "expira_em",
            nullable = false
    )
    private OffsetDateTime expiresAt;

    @Column(
            name = "usado_em"
    )
    private OffsetDateTime usedAt;

    @Column(
            name = "criado_em",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected PasswordResetToken() {
    }

    private PasswordResetToken(
            User user,
            String tokenHash,
            OffsetDateTime expiresAt
    ) {

        this.user = Objects.requireNonNull(
                user,
                "user não pode ser nulo"
        );

        this.tokenHash = Objects.requireNonNull(
                tokenHash,
                "tokenHash não pode ser nulo"
        );

        this.expiresAt = Objects.requireNonNull(
                expiresAt,
                "expiresAt não pode ser nulo"
        );
    }

    public static PasswordResetToken create(
            User user,
            String tokenHash,
            OffsetDateTime expiresAt
    ) {

        return new PasswordResetToken(
                user,
                tokenHash,
                expiresAt
        );
    }

    public void markAsUsed() {

        this.usedAt =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );
    }

    public boolean isUsed() {

        return usedAt != null;
    }

    public boolean isExpired() {

        return expiresAt.isBefore(
                OffsetDateTime.now(
                        ZoneOffset.UTC
                )
        );
    }

    @PrePersist
    private void prePersist() {

        if (createdAt == null) {

            createdAt =
                    OffsetDateTime.now(
                            ZoneOffset.UTC
                    );
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getUsedAt() {
        return usedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(
            Object object
    ) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof PasswordResetToken other)) {
            return false;
        }

        return id != null
                && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}