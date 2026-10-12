package br.com.maozinha.auth.dto;

import java.util.Objects;

public record RefreshTokenResponse(

        String accessToken,

        String refreshToken,

        Long expiresIn

) {

    public RefreshTokenResponse {

        Objects.requireNonNull(
                accessToken,
                "accessToken não pode ser nulo"
        );

        Objects.requireNonNull(
                refreshToken,
                "refreshToken não pode ser nulo"
        );

        Objects.requireNonNull(
                expiresIn,
                "expiresIn não pode ser nulo"
        );

        if (accessToken.isBlank()) {
            throw new IllegalArgumentException(
                    "accessToken não pode estar vazio"
            );
        }

        if (refreshToken.isBlank()) {
            throw new IllegalArgumentException(
                    "refreshToken não pode estar vazio"
            );
        }

        if (expiresIn <= 0) {
            throw new IllegalArgumentException(
                    "expiresIn deve ser maior que zero"
            );
        }
    }
}