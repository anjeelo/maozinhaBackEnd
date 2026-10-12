package br.com.maozinha.job;

import br.com.maozinha.category.Category;
import br.com.maozinha.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@Table(name = "servico")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "titulo",
            nullable = false,
            length = 120
    )
    private String title;

    @Column(
            name = "descricao",
            nullable = false,
            length = 2000
    )
    private String description;

    @Column(
            name = "valor",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal value;

    @Column(
            name = "data_servico",
            nullable = false
    )
    private LocalDate serviceDate;

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
            name = "status",
            nullable = false,
            length = 15
    )
    private JobStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "solicitante_id",
            nullable = false
    )
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "categoria_id",
            nullable = false
    )
    private Category category;

    @Column(
            name = "oculto_por_moderacao",
            nullable = false
    )
    private boolean hiddenByModeration;

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

    protected Job() {
    }

    private Job(
            String title,
            String description,
            BigDecimal value,
            LocalDate serviceDate,
            String city,
            String neighborhood,
            User requester,
            Category category
    ) {

        this.title = validateText(
                title,
                "O título",
                3,
                120
        );

        this.description = validateText(
                description,
                "A descrição",
                10,
                2000
        );

        Objects.requireNonNull(
                value,
                "O valor não pode ser nulo."
        );

        if (value.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "O valor deve ser maior que zero."
            );
        }

        this.value = value;

        this.serviceDate = Objects.requireNonNull(
                serviceDate,
                "A data do serviço não pode ser nula."
        );

        this.city = validateText(
                city,
                "A cidade",
                2,
                80
        );

        this.neighborhood = validateText(
                neighborhood,
                "O bairro",
                2,
                80
        );

        this.requester = Objects.requireNonNull(
                requester,
                "O solicitante não pode ser nulo."
        );

        this.category = Objects.requireNonNull(
                category,
                "A categoria não pode ser nula."
        );

        this.status = JobStatus.ABERTO;
        this.hiddenByModeration = false;
        this.version = 0L;
    }

    public static Job create(
            String title,
            String description,
            BigDecimal value,
            LocalDate serviceDate,
            String city,
            String neighborhood,
            User requester,
            Category category
    ) {

        return new Job(
                title,
                description,
                value,
                serviceDate,
                city,
                neighborhood,
                requester,
                category
        );
    }

    public void update(
            String title,
            String description,
            BigDecimal value,
            LocalDate serviceDate,
            String city,
            String neighborhood,
            Category category,
            JobStatus status
    ) {

        this.title = validateText(
                title,
                "O título",
                3,
                120
        );

        this.description = validateText(
                description,
                "A descrição",
                10,
                2000
        );

        this.value = value;

        this.serviceDate = serviceDate;

        this.city = validateText(
                city,
                "A cidade",
                2,
                80
        );

        this.neighborhood = validateText(
                neighborhood,
                "O bairro",
                2,
                80
        );

        this.category = category;

        this.status = Objects.requireNonNull(
                status,
                "O status não pode ser nulo."
        );
    }

    public void changeStatus(
            JobStatus status
    ) {

        this.status = Objects.requireNonNull(
                status,
                "O status não pode ser nulo."
        );
    }

    public void hideByModeration() {
        hiddenByModeration = true;
    }

    public void restoreByModeration() {
        hiddenByModeration = false;
    }

    @PrePersist
    private void prePersist() {

        OffsetDateTime now =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {

        updatedAt =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );
    }

    private static String validateText(
            String value,
            String fieldName,
            int min,
            int max
    ) {

        Objects.requireNonNull(
                value,
                fieldName + " não pode ser nulo."
        );

        String normalized =
                value.trim();

        if (normalized.length() < min
                || normalized.length() > max) {

            throw new IllegalArgumentException(
                    fieldName
                            + " deve possuir entre "
                            + min
                            + " e "
                            + max
                            + " caracteres."
            );
        }

        return normalized;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getValue() {
        return value;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public String getCity() {
        return city;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public JobStatus getStatus() {
        return status;
    }

    public User getRequester() {
        return requester;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isHiddenByModeration() {
        return hiddenByModeration;
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

    @Override
    public boolean equals(
            Object object
    ) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof Job other)) {
            return false;
        }

        return id != null
                && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}