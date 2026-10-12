package br.com.maozinha.job.dto;

import br.com.maozinha.job.Job;
import br.com.maozinha.job.JobStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

public record JobResponse(

        Long id,

        String title,

        String description,

        BigDecimal value,

        LocalDate serviceDate,

        String city,

        String neighborhood,

        JobStatus status,

        Long requesterId,

        String requesterName,

        Long categoryId,

        String categoryName,

        boolean hiddenByModeration,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {

    public JobResponse {

        Objects.requireNonNull(
                id,
                "id não pode ser nulo"
        );

        Objects.requireNonNull(
                title,
                "title não pode ser nulo"
        );

        Objects.requireNonNull(
                description,
                "description não pode ser nulo"
        );

        Objects.requireNonNull(
                value,
                "value não pode ser nulo"
        );

        Objects.requireNonNull(
                serviceDate,
                "serviceDate não pode ser nulo"
        );

        Objects.requireNonNull(
                city,
                "city não pode ser nulo"
        );

        Objects.requireNonNull(
                neighborhood,
                "neighborhood não pode ser nulo"
        );

        Objects.requireNonNull(
                status,
                "status não pode ser nulo"
        );

        Objects.requireNonNull(
                requesterId,
                "requesterId não pode ser nulo"
        );

        Objects.requireNonNull(
                requesterName,
                "requesterName não pode ser nulo"
        );

        Objects.requireNonNull(
                categoryId,
                "categoryId não pode ser nulo"
        );

        Objects.requireNonNull(
                categoryName,
                "categoryName não pode ser nulo"
        );

        Objects.requireNonNull(
                createdAt,
                "createdAt não pode ser nulo"
        );

        Objects.requireNonNull(
                updatedAt,
                "updatedAt não pode ser nulo"
        );
    }

    public static JobResponse from(
            Job job
    ) {

        Objects.requireNonNull(
                job,
                "job não pode ser nulo"
        );

        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getValue(),
                job.getServiceDate(),
                job.getCity(),
                job.getNeighborhood(),
                job.getStatus(),
                job.getRequester().getId(),
                job.getRequester().getName(),
                job.getCategory().getId(),
                job.getCategory().getName(),
                job.isHiddenByModeration(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}