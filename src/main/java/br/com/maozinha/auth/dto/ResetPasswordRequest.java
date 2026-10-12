package br.com.maozinha.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

        @NotBlank(
                message = "O token de recuperação é obrigatório."
        )
        String token,

        @NotBlank(
                message = "A nova senha é obrigatória."
        )
        @Size(
                min = 8,
                max = 72,
                message = "A senha deve possuir entre 8 e 72 caracteres."
        )
        String newPassword

) {
}