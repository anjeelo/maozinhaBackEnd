package br.com.maozinha.auth.dto;

import java.util.Objects;

public record ForgotPasswordResponse(

        String message

) {

    public ForgotPasswordResponse {

        Objects.requireNonNull(
                message,
                "message não pode ser nulo"
        );

        if (message.isBlank()) {
            throw new IllegalArgumentException(
                    "message não pode estar vazia"
            );
        }
    }

    public static ForgotPasswordResponse success() {

        return new ForgotPasswordResponse(
                "Se existir um usuário com este e-mail, as instruções de recuperação foram geradas."
        );
    }
}