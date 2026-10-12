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
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
public class RefreshToken {

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
            name = "familia",
            nullable = false
    )
    private UUID family;

    @Column(
            name = "expira_em",
            nullable = false
    )
    private OffsetDateTime expiresAt;

    @Column(
            name = "revogado_em"
    )
    private OffsetDateTime revokedAt;

    @Column(
            name = "criado_em",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected RefreshToken() {
    }

    private RefreshToken(
            User user,
            String tokenHash,
            UUID family,
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

        this.family = Objects.requireNonNull(
                family,
                "family não pode ser nulo"
        );

        this.expiresAt = Objects.requireNonNull(
                expiresAt,
                "expiresAt não pode ser nulo"
        );
    }

    public static RefreshToken create(
            User user,
            String tokenHash,
            UUID family,
            OffsetDateTime expiresAt
    ) {

        return new RefreshToken(
                user,
                tokenHash,
                family,
                expiresAt
        );
    }

    public void revoke() {

        this.revokedAt =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );
    }

    public boolean isRevoked() {

        return revokedAt != null;
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

    public UUID getFamily() {
        return family;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
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

        if (!(object instanceof RefreshToken other)) {
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