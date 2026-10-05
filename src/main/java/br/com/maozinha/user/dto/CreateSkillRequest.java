package br.com.maozinha.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSkillRequest(

        @NotBlank(message = "O nome da habilidade é obrigatório.")
        @Size(
                min = 2,
                max = 60,
                message = "O nome da habilidade deve possuir entre 2 e 60 caracteres."
        )
        String name
) {
}