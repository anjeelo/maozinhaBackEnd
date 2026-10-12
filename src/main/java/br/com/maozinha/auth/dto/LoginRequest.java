package br.com.maozinha.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail informado é inválido.")
        @Size(
                min = 5,
                max = 254,
                message = "O e-mail deve possuir entre 5 e 254 caracteres."
        )
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(
                min = 8,
                max = 72,
                message = "A senha deve possuir entre 8 e 72 caracteres."
        )
        String password

) {
}