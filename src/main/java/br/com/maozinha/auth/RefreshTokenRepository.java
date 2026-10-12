package br.com.maozinha.auth;

import br.com.maozinha.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(
            String tokenHash
    );

    List<RefreshToken> findByUser(
            User user
    );

    List<RefreshToken> findByFamily(
            UUID family
    );

    List<RefreshToken> findByUserAndRevokedAtIsNull(
            User user
    );

    List<RefreshToken> findByExpiresAtBefore(
            OffsetDateTime moment
    );

    boolean existsByTokenHash(
            String tokenHash
    );
}