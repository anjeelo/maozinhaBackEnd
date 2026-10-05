package br.com.maozinha.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(
                min = 2,
                max = 120,
                message = "O nome deve possuir entre 2 e 120 caracteres."
        )
        String name,

        @NotBlank(message = "O WhatsApp é obrigatório.")
        @Size(
                min = 8,
                max = 20,
                message = "O WhatsApp deve possuir entre 8 e 20 caracteres."
        )
        @Pattern(
                regexp = "^[0-9+()\\-\\s]+$",
                message = "O WhatsApp possui caracteres inválidos."
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