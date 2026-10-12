package br.com.maozinha.auth.dto;

import java.time.OffsetDateTime;

public record RegisterResponse(

        Long id,
        String name,
        String email,
        OffsetDateTime createdAt

) {
}