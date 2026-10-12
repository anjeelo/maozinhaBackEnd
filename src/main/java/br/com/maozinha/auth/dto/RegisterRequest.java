package br.com.maozinha.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(
                min = 2,
                max = 120,
                message = "O nome deve possuir entre 2 e 120 caracteres."
        )
        String name,

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
        String password,

        @NotBlank(message = "O WhatsApp é obrigatório.")
        @Size(
                min = 8,
                max = 20,
                message = "O WhatsApp deve possuir entre 8 e 20 caracteres."
        )
        String whatsapp,

        @NotBlank(message = "A cidade é obrigatória.")
        @Size(
                min = 2,
                max = 80,
                message = "A cidade deve possuir entre 2 e 80 caracteres."
        )
        String city,

        @NotBlank(message = "O bairro é obrigatório.")
        @Size(
                min = 2,
                max = 80,
                message = "O bairro deve possuir entre 2 e 80 caracteres."
        )
        String neighborhood

) {
}