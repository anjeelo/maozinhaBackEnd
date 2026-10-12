package br.com.maozinha.auth.dto;

import java.util.Objects;

public record AuthResponse(

        String accessToken,
        Long expiresIn

) {

    public AuthResponse {

        Objects.requireNonNull(
                accessToken,
                "accessToken não pode ser nulo"
        );

        Objects.requireNonNull(
                expiresIn,
                "expiresIn não pode ser nulo"
        );
    }
}