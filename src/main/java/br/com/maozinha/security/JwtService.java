package br.com.maozinha.security;

import br.com.maozinha.common.config.JwtProperties;
import br.com.maozinha.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(
            JwtProperties jwtProperties
    ) {

        this.jwtProperties = Objects.requireNonNull(
                jwtProperties,
                "jwtProperties não pode ser nulo"
        );

        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateAccessToken(
            Long userId,
            String email,
            Role role
    ) {

        Instant now = Instant.now();

        Instant expiration = now.plus(
                jwtProperties.accessTokenExpiration()
        );

        return Jwts.builder()
                .subject(email)
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim("userId", userId)
                .claim("role", role.name())
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(
            String token
    ) {

        return extractClaims(token)
                .getSubject();
    }

    public boolean isTokenValid(
            String token
    ) {

        try {

            extractClaims(token);

            return true;

        } catch (Exception exception) {

            return false;
        }
    }

    public long getAccessTokenExpirationSeconds() {

        return jwtProperties
                .accessTokenExpiration()
                .toSeconds();
    }

    private Claims extractClaims(
            String token
    ) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}