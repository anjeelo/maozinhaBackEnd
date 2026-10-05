package br.com.maozinha.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "habilidade")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "nome",
            nullable = false,
            length = 60
    )
    private String name;

    protected Skill() {
    }

    private Skill(String name) {
        this.name = normalizeName(name);
    }

    public static Skill create(String name) {
        return new Skill(name);
    }

    public void rename(String name) {
        this.name = normalizeName(name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private static String normalizeName(String name) {
        Objects.requireNonNull(name, "O nome da habilidade não pode ser nulo.");

        String normalizedName = name.trim();

        if (normalizedName.length() < 2 || normalizedName.length() > 60) {
            throw new IllegalArgumentException(
                    "O nome da habilidade deve possuir entre 2 e 60 caracteres."
            );
        }

        return normalizedName;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Skill other)) {
            return false;
        }

        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}