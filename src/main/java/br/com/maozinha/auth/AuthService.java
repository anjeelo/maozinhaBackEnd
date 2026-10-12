package br.com.maozinha.auth;

import br.com.maozinha.auth.dto.ForgotPasswordRequest;
import br.com.maozinha.auth.dto.ForgotPasswordResponse;
import br.com.maozinha.auth.dto.LoginRequest;
import br.com.maozinha.auth.dto.RefreshTokenRequest;
import br.com.maozinha.auth.dto.RefreshTokenResponse;
import br.com.maozinha.auth.dto.RegisterRequest;
import br.com.maozinha.auth.dto.RegisterResponse;
import br.com.maozinha.auth.dto.ResetPasswordRequest;
import br.com.maozinha.auth.dto.ResetPasswordResponse;
import br.com.maozinha.common.config.JwtProperties;
import br.com.maozinha.common.exception.ConflictException;
import br.com.maozinha.common.exception.ForbiddenException;
import br.com.maozinha.common.exception.NotFoundException;
import br.com.maozinha.security.JwtService;
import br.com.maozinha.user.User;
import br.com.maozinha.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class AuthService {

    private static final int TOKEN_SIZE_BYTES = 32;

    private static final Duration PASSWORD_RESET_EXPIRATION =
            Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetEmailService passwordResetEmailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final SecureRandom secureRandom;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordResetEmailService passwordResetEmailService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties,
            Clock clock
    ) {
        this.userRepository = Objects.requireNonNull(
                userRepository,
                "userRepository não pode ser nulo"
        );

        this.refreshTokenRepository = Objects.requireNonNull(
                refreshTokenRepository,
                "refreshTokenRepository não pode ser nulo"
        );

        this.passwordResetTokenRepository = Objects.requireNonNull(
                passwordResetTokenRepository,
                "passwordResetTokenRepository não pode ser nulo"
        );

        this.passwordResetEmailService = Objects.requireNonNull(
                passwordResetEmailService,
                "passwordResetEmailService não pode ser nulo"
        );

        this.passwordEncoder = Objects.requireNonNull(
                passwordEncoder,
                "passwordEncoder não pode ser nulo"
        );

        this.authenticationManager = Objects.requireNonNull(
                authenticationManager,
                "authenticationManager não pode ser nulo"
        );

        this.jwtService = Objects.requireNonNull(
                jwtService,
                "jwtService não pode ser nulo"
        );

        this.jwtProperties = Objects.requireNonNull(
                jwtProperties,
                "jwtProperties não pode ser nulo"
        );

        this.clock = Objects.requireNonNull(
                clock,
                "clock não pode ser nulo"
        );

        this.secureRandom = new SecureRandom();
    }

    @Transactional
    public RegisterResponse register(
            RegisterRequest request
    ) {
        Objects.requireNonNull(
                request,
                "request não pode ser nulo"
        );

        String normalizedEmail = normalizeEmail(
                request.email()
        );

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException(
                    "Já existe um usuário cadastrado com este e-mail."
            );
        }

        String passwordHash = passwordEncoder.encode(
                request.password()
        );

        User user = User.create(
                request.name(),
                normalizedEmail,
                passwordHash,
                request.whatsapp(),
                request.city(),
                request.neighborhood()
        );

        User savedUser = userRepository.saveAndFlush(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );
    }

    @Transactional
    public RefreshTokenResponse login(
            LoginRequest request
    ) {
        Objects.requireNonNull(
                request,
                "request não pode ser nulo"
        );

        String normalizedEmail = normalizeEmail(
                request.email()
        );

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.password()
                )
        );

        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Usuário não encontrado."
                        )
                );

        if (!user.isActive()) {
            throw new ForbiddenException(
                    "O usuário não está ativo."
            );
        }

        revokeActiveRefreshTokens(user);

        return createAuthenticationResponse(
                user,
                UUID.randomUUID()
        );
    }

    @Transactional
    public RefreshTokenResponse refresh(
            RefreshTokenRequest request
    ) {
        Objects.requireNonNull(
                request,
                "request não pode ser nulo"
        );

        String tokenHash = hashToken(
                request.refreshToken()
        );

        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new ForbiddenException(
                                "Refresh token inválido."
                        )
                );

        if (storedToken.isRevoked()) {
            revokeRefreshTokenFamily(
                    storedToken.getFamily()
            );

            throw new ForbiddenException(
                    "Refresh token revogado."
            );
        }

        if (storedToken.isExpired()) {
            storedToken.revoke();

            refreshTokenRepository.saveAndFlush(storedToken);

            throw new ForbiddenException(
                    "Refresh token expirado."
            );
        }

        User user = storedToken.getUser();

        if (!user.isActive()) {
            storedToken.revoke();

            refreshTokenRepository.saveAndFlush(storedToken);

            throw new ForbiddenException(
                    "O usuário não está ativo."
            );
        }

        storedToken.revoke();

        refreshTokenRepository.saveAndFlush(storedToken);

        return createAuthenticationResponse(
                user,
                storedToken.getFamily()
        );
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(
            ForgotPasswordRequest request
    ) {
        Objects.requireNonNull(
                request,
                "request não pode ser nulo"
        );

        String normalizedEmail = normalizeEmail(
                request.email()
        );

        userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .filter(User::isActive)
                .ifPresent(this::createPasswordResetToken);

        return ForgotPasswordResponse.success();
    }

    @Transactional
    public ResetPasswordResponse resetPassword(
            ResetPasswordRequest request
    ) {
        Objects.requireNonNull(
                request,
                "request não pode ser nulo"
        );

        String tokenHash = hashToken(
                request.token()
        );

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new ForbiddenException(
                                "Token de recuperação inválido."
                        )
                );

        if (resetToken.isUsed()) {
            throw new ForbiddenException(
                    "Token de recuperação já utilizado."
            );
        }

        if (resetToken.isExpired()) {
            throw new ForbiddenException(
                    "Token de recuperação expirado."
            );
        }

        User user = resetToken.getUser();

        if (!user.isActive()) {
            throw new ForbiddenException(
                    "O usuário não está ativo."
            );
        }

        String newPasswordHash = passwordEncoder.encode(
                request.newPassword()
        );

        user.changePasswordHash(newPasswordHash);

        resetToken.markAsUsed();

        userRepository.save(user);

        passwordResetTokenRepository.saveAndFlush(resetToken);

        revokeActiveRefreshTokens(user);

        invalidateUnusedPasswordResetTokens(
                user,
                resetToken.getId()
        );

        return ResetPasswordResponse.success();
    }

    private RefreshTokenResponse createAuthenticationResponse(
            User user,
            UUID family
    ) {
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        String rawRefreshToken = generateRawToken();

        RefreshToken refreshToken = RefreshToken.create(
                user,
                hashToken(rawRefreshToken),
                family,
                now().plus(
                        jwtProperties.refreshTokenExpiration()
                )
        );

        refreshTokenRepository.saveAndFlush(refreshToken);

        return new RefreshTokenResponse(
                accessToken,
                rawRefreshToken,
                jwtService.getAccessTokenExpirationSeconds()
        );
    }

    private void createPasswordResetToken(
            User user
    ) {
        invalidateUnusedPasswordResetTokens(
                user,
                null
        );

        String rawToken = generateRawToken();

        PasswordResetToken resetToken = PasswordResetToken.create(
                user,
                hashToken(rawToken),
                now().plus(PASSWORD_RESET_EXPIRATION)
        );

        passwordResetTokenRepository.saveAndFlush(resetToken);

        passwordResetEmailService.sendResetToken(
                user.getEmail(),
                rawToken
        );
    }

    private void invalidateUnusedPasswordResetTokens(
            User user,
            Long tokenToKeepId
    ) {
        List<PasswordResetToken> tokens = passwordResetTokenRepository
                .findByUserAndUsedAtIsNull(user);

        tokens.stream()
                .filter(token ->
                        tokenToKeepId == null
                                || !Objects.equals(
                                token.getId(),
                                tokenToKeepId
                        )
                )
                .filter(token -> !token.isUsed())
                .forEach(PasswordResetToken::markAsUsed);

        if (!tokens.isEmpty()) {
            passwordResetTokenRepository.saveAll(tokens);
            passwordResetTokenRepository.flush();
        }
    }

    private void revokeActiveRefreshTokens(
            User user
    ) {
        List<RefreshToken> activeTokens = refreshTokenRepository
                .findByUserAndRevokedAtIsNull(user);

        activeTokens.stream()
                .filter(token -> !token.isRevoked())
                .forEach(RefreshToken::revoke);

        if (!activeTokens.isEmpty()) {
            refreshTokenRepository.saveAll(activeTokens);
            refreshTokenRepository.flush();
        }
    }

    private void revokeRefreshTokenFamily(
            UUID family
    ) {
        List<RefreshToken> familyTokens = refreshTokenRepository
                .findByFamily(family);

        familyTokens.stream()
                .filter(token -> !token.isRevoked())
                .forEach(RefreshToken::revoke);

        if (!familyTokens.isEmpty()) {
            refreshTokenRepository.saveAll(familyTokens);
            refreshTokenRepository.flush();
        }
    }

    private String generateRawToken() {
        byte[] randomBytes = new byte[TOKEN_SIZE_BYTES];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hashToken(
            String rawToken
    ) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ForbiddenException(
                    "Token inválido."
            );
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(
                    "SHA-256"
            );

            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Não foi possível gerar o hash do token.",
                    exception
            );
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(
                Instant.now(clock),
                ZoneOffset.UTC
        );
    }

    private String normalizeEmail(
            String email
    ) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "O e-mail é obrigatório."
            );
        }

        return email.trim()
                .toLowerCase(Locale.ROOT);
    }
}