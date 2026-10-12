package br.com.maozinha.admin;

import br.com.maozinha.auth.RefreshToken;
import br.com.maozinha.auth.RefreshTokenRepository;
import br.com.maozinha.common.exception.ConflictException;
import br.com.maozinha.common.exception.ForbiddenException;
import br.com.maozinha.common.exception.NotFoundException;
import br.com.maozinha.user.User;
import br.com.maozinha.user.UserRepository;
import br.com.maozinha.user.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ModerationActionRepository moderationActionRepository;

    public AdminService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            ModerationActionRepository moderationActionRepository
    ) {
        this.userRepository = Objects.requireNonNull(
                userRepository,
                "userRepository não pode ser nulo"
        );

        this.refreshTokenRepository = Objects.requireNonNull(
                refreshTokenRepository,
                "refreshTokenRepository não pode ser nulo"
        );

        this.moderationActionRepository = Objects.requireNonNull(
                moderationActionRepository,
                "moderationActionRepository não pode ser nulo"
        );
    }

    @Transactional
    public void suspendUser(
            Long userId,
            String authenticatedEmail
    ) {
        suspendUser(
                userId,
                authenticatedEmail,
                "Suspensão administrativa sem motivo específico informado."
        );
    }

    @Transactional
    public void suspendUser(
            Long userId,
            String authenticatedEmail,
            String reason
    ) {
        User administrator = findActiveAdministrator(
                authenticatedEmail
        );

        User user = findUserById(
                userId
        );

        if (Objects.equals(
                administrator.getId(),
                user.getId()
        )) {
            throw new ConflictException(
                    "O administrador não pode suspender a própria conta."
            );
        }

        ensureNotDeleted(
                user
        );

        if (user.getStatus() == UserStatus.SUSPENSO) {
            return;
        }

        ModerationAction audit = ModerationAction.create(
                administrator,
                ModerationTargetType.USUARIO,
                user.getId(),
                ModerationActionType.SUSPENDER_USUARIO,
                reason
        );

        user.suspend();

        revokeActiveRefreshTokens(
                user
        );

        userRepository.saveAndFlush(
                user
        );

        moderationActionRepository.saveAndFlush(
                audit
        );
    }

    @Transactional
    public void reactivateUser(
            Long userId,
            String authenticatedEmail
    ) {
        reactivateUser(
                userId,
                authenticatedEmail,
                "Reativação administrativa sem motivo específico informado."
        );
    }

    @Transactional
    public void reactivateUser(
            Long userId,
            String authenticatedEmail,
            String reason
    ) {
        User administrator = findActiveAdministrator(
                authenticatedEmail
        );

        User user = findUserById(
                userId
        );

        ensureNotDeleted(
                user
        );

        if (user.isActive()) {
            return;
        }

        ModerationAction audit = ModerationAction.create(
                administrator,
                ModerationTargetType.USUARIO,
                user.getId(),
                ModerationActionType.REATIVAR_USUARIO,
                reason
        );

        user.reactivate();

        userRepository.saveAndFlush(
                user
        );

        moderationActionRepository.saveAndFlush(
                audit
        );
    }

    private User findActiveAdministrator(
            String authenticatedEmail
    ) {
        if (authenticatedEmail == null
                || authenticatedEmail.isBlank()) {
            throw new ForbiddenException(
                    "É necessário autenticar-se como administrador."
            );
        }

        String normalizedEmail = authenticatedEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        User administrator = userRepository
                .findByEmailIgnoreCase(
                        normalizedEmail
                )
                .orElseThrow(() ->
                        new ForbiddenException(
                                "Acesso administrativo não autorizado."
                        )
                );

        if (!administrator.isActive()
                || !administrator.isAdmin()) {
            throw new ForbiddenException(
                    "Somente administradores ativos podem executar esta ação."
            );
        }

        return administrator;
    }

    private User findUserById(
            Long userId
    ) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "O identificador do usuário deve ser maior que zero."
            );
        }

        return userRepository
                .findById(
                        userId
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "Usuário não encontrado."
                        )
                );
    }

    private void ensureNotDeleted(
            User user
    ) {
        if (user.getStatus() == UserStatus.EXCLUIDO) {
            throw new ConflictException(
                    "Um usuário excluído não pode ser suspenso ou reativado."
            );
        }
    }

    private void revokeActiveRefreshTokens(
            User user
    ) {
        List<RefreshToken> tokens = refreshTokenRepository
                .findByUserAndRevokedAtIsNull(
                        user
                );

        tokens.forEach(
                RefreshToken::revoke
        );

        if (!tokens.isEmpty()) {
            refreshTokenRepository.saveAll(
                    tokens
            );

            refreshTokenRepository.flush();
        }
    }
}