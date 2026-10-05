package br.com.maozinha.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "usuario")
public class User {

    private static final BigDecimal MINIMUM_RATING = BigDecimal.ZERO;
    private static final BigDecimal MAXIMUM_RATING = new BigDecimal("5.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "nome",
            nullable = false,
            length = 120
    )
    private String name;

    @Column(
            name = "email",
            nullable = false,
            length = 254
    )
    private String email;

    @Column(
            name = "senha_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;

    @Column(
            name = "whatsapp",
            nullable = false,
            length = 20
    )
    private String whatsapp;

    @Column(
            name = "cidade",
            nullable = false,
            length = 80
    )
    private String city;

    @Column(
            name = "bairro",
            nullable = false,
            length = 80
    )
    private String neighborhood;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "papel",
            nullable = false,
            length = 10
    )
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "situacao",
            nullable = false,
            length = 10
    )
    private UserStatus status;

    @Column(
            name = "reputacao_media",
            nullable = false,
            precision = 3,
            scale = 2
    )
    private BigDecimal averageRating;

    @Column(
            name = "total_avaliacoes",
            nullable = false
    )
    private Integer totalReviews;

    @Column(
            name = "criado_em",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "atualizado_em",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @Version
    @Column(
            name = "versao",
            nullable = false
    )
    private Long version;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_habilidade",
            joinColumns = @JoinColumn(
                    name = "usuario_id",
                    nullable = false
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "habilidade_id",
                    nullable = false
            )
    )
    private Set<Skill> skills = new LinkedHashSet<>();

    protected User() {
    }

    private User(
            String name,
            String email,
            String passwordHash,
            String whatsapp,
            String city,
            String neighborhood
    ) {
        this.name = normalizeRequiredText(
                name,
                "O nome do usuário",
                2,
                120
        );
        this.email = normalizeEmail(email);
        this.passwordHash = validatePasswordHash(passwordHash);
        this.whatsapp = normalizeRequiredText(
                whatsapp,
                "O WhatsApp",
                8,
                20
        );
        this.city = normalizeRequiredText(
                city,
                "A cidade",
                2,
                80
        );
        this.neighborhood = normalizeRequiredText(
                neighborhood,
                "O bairro",
                2,
                80
        );
        this.role = Role.USUARIO;
        this.status = UserStatus.ATIVO;
        this.averageRating = BigDecimal.ZERO.setScale(
                2,
                RoundingMode.HALF_UP
        );
        this.totalReviews = 0;
        this.version = 0L;
        this.skills = new LinkedHashSet<>();
    }

    public static User create(
            String name,
            String email,
            String passwordHash,
            String whatsapp,
            String city,
            String neighborhood
    ) {
        return new User(
                name,
                email,
                passwordHash,
                whatsapp,
                city,
                neighborhood
        );
    }

    public void updateProfile(
            String name,
            String whatsapp,
            String city,
            String neighborhood
    ) {
        this.name = normalizeRequiredText(
                name,
                "O nome do usuário",
                2,
                120
        );
        this.whatsapp = normalizeRequiredText(
                whatsapp,
                "O WhatsApp",
                8,
                20
        );
        this.city = normalizeRequiredText(
                city,
                "A cidade",
                2,
                80
        );
        this.neighborhood = normalizeRequiredText(
                neighborhood,
                "O bairro",
                2,
                80
        );
    }

    public void changeEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = validatePasswordHash(passwordHash);
    }

    public void addSkill(Skill skill) {
        Objects.requireNonNull(
                skill,
                "A habilidade não pode ser nula."
        );

        skills.add(skill);
    }

    public void removeSkill(Skill skill) {
        Objects.requireNonNull(
                skill,
                "A habilidade não pode ser nula."
        );

        skills.remove(skill);
    }

    public void suspend() {
        ensureNotDeleted();

        if (status == UserStatus.SUSPENSO) {
            return;
        }

        status = UserStatus.SUSPENSO;
    }

    public void reactivate() {
        ensureNotDeleted();
        status = UserStatus.ATIVO;
    }

    public void delete() {
        status = UserStatus.EXCLUIDO;
        skills.clear();
    }

    public void promoteToAdmin() {
        ensureNotDeleted();
        role = Role.ADMIN;
    }

    public void changeReputation(
            BigDecimal averageRating,
            int totalReviews
    ) {
        Objects.requireNonNull(
                averageRating,
                "A reputação média não pode ser nula."
        );

        BigDecimal normalizedRating = averageRating.setScale(
                2,
                RoundingMode.HALF_UP
        );

        if (normalizedRating.compareTo(MINIMUM_RATING) < 0
                || normalizedRating.compareTo(MAXIMUM_RATING) > 0) {
            throw new IllegalArgumentException(
                    "A reputação média deve estar entre 0,00 e 5,00."
            );
        }

        if (totalReviews < 0) {
            throw new IllegalArgumentException(
                    "O total de avaliações não pode ser negativo."
            );
        }

        if (totalReviews == 0
                && normalizedRating.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(
                    "Um usuário sem avaliações deve possuir reputação média igual a 0,00."
            );
        }

        this.averageRating = normalizedRating;
        this.totalReviews = totalReviews;
    }

    public boolean isActive() {
        return status == UserStatus.ATIVO;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @PrePersist
    private void prePersist() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (role == null) {
            role = Role.USUARIO;
        }

        if (status == null) {
            status = UserStatus.ATIVO;
        }

        if (averageRating == null) {
            averageRating = BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        if (totalReviews == null) {
            totalReviews = 0;
        }

        if (version == null) {
            version = 0L;
        }

        if (skills == null) {
            skills = new LinkedHashSet<>();
        }

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    private void ensureNotDeleted() {
        if (status == UserStatus.EXCLUIDO) {
            throw new IllegalStateException(
                    "Um usuário excluído não pode ter sua situação alterada."
            );
        }
    }

    private static String normalizeEmail(String email) {
        String normalizedEmail = normalizeRequiredText(
                email,
                "O e-mail",
                5,
                254
        ).toLowerCase(Locale.ROOT);

        int atIndex = normalizedEmail.indexOf('@');

        if (atIndex <= 0 || atIndex == normalizedEmail.length() - 1) {
            throw new IllegalArgumentException(
                    "O e-mail informado é inválido."
            );
        }

        return normalizedEmail;
    }

    private static String validatePasswordHash(String passwordHash) {
        return normalizeRequiredText(
                passwordHash,
                "O hash da senha",
                20,
                255
        );
    }

    private static String normalizeRequiredText(
            String value,
            String fieldName,
            int minimumLength,
            int maximumLength
    ) {
        Objects.requireNonNull(
                value,
                fieldName + " não pode ser nulo."
        );

        String normalizedValue = value.trim();

        if (normalizedValue.length() < minimumLength
                || normalizedValue.length() > maximumLength) {
            throw new IllegalArgumentException(
                    fieldName
                            + " deve possuir entre "
                            + minimumLength
                            + " e "
                            + maximumLength
                            + " caracteres."
            );
        }

        return normalizedValue;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public String getCity() {
        return city;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public Set<Skill> getSkills() {
        return Collections.unmodifiableSet(skills);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof User other)) {
            return false;
        }

        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}