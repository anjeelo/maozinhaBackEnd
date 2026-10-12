package br.com.maozinha.auth;

import br.com.maozinha.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(
            String tokenHash
    );

    boolean existsByTokenHash(
            String tokenHash
    );

    List<PasswordResetToken> findByUser(
            User user
    );

    List<PasswordResetToken> findByUserAndUsedAtIsNull(
            User user
    );

    List<PasswordResetToken> findByExpiresAtBefore(
            OffsetDateTime moment
    );
}