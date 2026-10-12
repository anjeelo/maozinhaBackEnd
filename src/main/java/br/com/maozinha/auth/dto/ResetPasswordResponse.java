package br.com.maozinha.auth.dto;

import java.util.Objects;

public record ResetPasswordResponse(

        String message

) {

    public ResetPasswordResponse {

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

    public static ResetPasswordResponse success() {

        return new ResetPasswordResponse(
                "Senha alterada com sucesso."
        );
    }
}