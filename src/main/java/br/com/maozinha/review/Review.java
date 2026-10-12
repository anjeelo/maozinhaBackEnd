package br.com.maozinha.review;

import br.com.maozinha.job.Job;
import br.com.maozinha.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@Table(
        name = "avaliacao",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux_avaliacao_servico_par",
                        columnNames = {
                                "servico_id",
                                "avaliador_id",
                                "avaliado_id"
                        }
                )
        }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "nota",
            nullable = false
    )
    private Short rating;

    @Column(
            name = "comentario",
            length = 500
    )
    private String comment;

    @Column(
            name = "data_avaliacao",
            nullable = false
    )
    private OffsetDateTime reviewDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "avaliador_id",
            nullable = false
    )
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "avaliado_id",
            nullable = false
    )
    private User reviewed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "servico_id",
            nullable = false
    )
    private Job job;

    @Column(
            name = "removida_por_moderacao",
            nullable = false
    )
    private boolean removedByModeration;

    @Column(
            name = "removida_em"
    )
    private OffsetDateTime removedAt;

    protected Review() {
    }

    private Review(
            Short rating,
            String comment,
            User reviewer,
            User reviewed,
            Job job
    ) {

        validateRating(rating);

        this.rating = rating;

        this.comment = normalizeComment(
                comment
        );

        this.reviewer = Objects.requireNonNull(
                reviewer,
                "reviewer não pode ser nulo"
        );

        this.reviewed = Objects.requireNonNull(
                reviewed,
                "reviewed não pode ser nulo"
        );

        this.job = Objects.requireNonNull(
                job,
                "job não pode ser nulo"
        );

        if (reviewer.getId() != null
                && reviewer.getId().equals(
                reviewed.getId()
        )) {

            throw new IllegalArgumentException(
                    "O usuário não pode avaliar a si mesmo."
            );
        }

        this.removedByModeration = false;
    }

    public static Review create(
            Short rating,
            String comment,
            User reviewer,
            User reviewed,
            Job job
    ) {

        return new Review(
                rating,
                comment,
                reviewer,
                reviewed,
                job
        );
    }

    public void removeByModeration() {

        this.removedByModeration = true;

        this.removedAt =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );
    }

    public void restore() {

        this.removedByModeration = false;
        this.removedAt = null;
    }

    @PrePersist
    private void prePersist() {

        reviewDate =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );
    }

    private static void validateRating(
            Short rating
    ) {

        Objects.requireNonNull(
                rating,
                "A nota não pode ser nula."
        );

        if (rating < 1 || rating > 5) {

            throw new IllegalArgumentException(
                    "A nota deve estar entre 1 e 5."
            );
        }
    }

    private static String normalizeComment(
            String comment
    ) {

        if (comment == null) {
            return null;
        }

        String normalized =
                comment.trim();

        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.length() > 500) {

            throw new IllegalArgumentException(
                    "O comentário não pode ultrapassar 500 caracteres."
            );
        }

        return normalized;
    }

    public Long getId() {
        return id;
    }

    public Short getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public OffsetDateTime getReviewDate() {
        return reviewDate;
    }

    public User getReviewer() {
        return reviewer;
    }

    public User getReviewed() {
        return reviewed;
    }

    public Job getJob() {
        return job;
    }

    public boolean isRemovedByModeration() {
        return removedByModeration;
    }

    public OffsetDateTime getRemovedAt() {
        return removedAt;
    }

    @Override
    public boolean equals(
            Object object
    ) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof Review other)) {
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