package br.com.maozinha.review.dto;

import br.com.maozinha.review.Review;

import java.time.OffsetDateTime;
import java.util.Objects;

public record ReviewResponse(

        Long id,

        Short rating,

        String comment,

        OffsetDateTime reviewDate,

        Long reviewerId,

        String reviewerName,

        Long reviewedId,

        String reviewedName,

        Long jobId,

        String jobTitle,

        boolean removedByModeration

) {

    public ReviewResponse {

        Objects.requireNonNull(
                id,
                "id não pode ser nulo"
        );

        Objects.requireNonNull(
                rating,
                "rating não pode ser nulo"
        );

        Objects.requireNonNull(
                reviewDate,
                "reviewDate não pode ser nulo"
        );

        Objects.requireNonNull(
                reviewerId,
                "reviewerId não pode ser nulo"
        );

        Objects.requireNonNull(
                reviewerName,
                "reviewerName não pode ser nulo"
        );

        Objects.requireNonNull(
                reviewedId,
                "reviewedId não pode ser nulo"
        );

        Objects.requireNonNull(
                reviewedName,
                "reviewedName não pode ser nulo"
        );

        Objects.requireNonNull(
                jobId,
                "jobId não pode ser nulo"
        );

        Objects.requireNonNull(
                jobTitle,
                "jobTitle não pode ser nulo"
        );
    }

    public static ReviewResponse from(
            Review review
    ) {

        Objects.requireNonNull(
                review,
                "review não pode ser nulo"
        );

        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getReviewDate(),
                review.getReviewer().getId(),
                review.getReviewer().getName(),
                review.getReviewed().getId(),
                review.getReviewed().getName(),
                review.getJob().getId(),
                review.getJob().getTitle(),
                review.isRemovedByModeration()
        );
    }
}