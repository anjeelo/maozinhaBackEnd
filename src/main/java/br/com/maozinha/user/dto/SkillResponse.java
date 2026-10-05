package br.com.maozinha.user.dto;

import br.com.maozinha.user.Skill;

import java.util.Objects;

public record SkillResponse(

        Long id,
        String name
) {

    public SkillResponse {
        Objects.requireNonNull(
                id,
                "O identificador da habilidade não pode ser nulo."
        );

        Objects.requireNonNull(
                name,
                "O nome da habilidade não pode ser nulo."
        );
    }

    public static SkillResponse from(Skill skill) {
        Objects.requireNonNull(
                skill,
                "A habilidade não pode ser nula."
        );

        return new SkillResponse(
                skill.getId(),
                skill.getName()
        );
    }
}