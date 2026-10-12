package br.com.maozinha.interest.dto;

import br.com.maozinha.interest.Interest;
import br.com.maozinha.interest.InterestStatus;

import java.time.OffsetDateTime;
import java.util.Objects;

public record InterestResponse(

        Long id,

        InterestStatus status,

        Long userId,

        String userName,

        Long jobId,

        String jobTitle,

        OffsetDateTime interestDate

) {

    public InterestResponse {

        Objects.requireNonNull(
                id,
                "id não pode ser nulo"
        );

        Objects.requireNonNull(
                status,
                "status não pode ser nulo"
        );

        Objects.requireNonNull(
                userId,
                "userId não pode ser nulo"
        );

        Objects.requireNonNull(
                userName,
                "userName não pode ser nulo"
        );

        Objects.requireNonNull(
                jobId,
                "jobId não pode ser nulo"
        );

        Objects.requireNonNull(
                jobTitle,
                "jobTitle não pode ser nulo"
        );

        Objects.requireNonNull(
                interestDate,
                "interestDate não pode ser nulo"
        );
    }

    public static InterestResponse from(
            Interest interest
    ) {

        Objects.requireNonNull(
                interest,
                "interest não pode ser nulo"
        );

        return new InterestResponse(
                interest.getId(),
                interest.getStatus(),
                interest.getUser().getId(),
                interest.getUser().getName(),
                interest.getJob().getId(),
                interest.getJob().getTitle(),
                interest.getInterestDate()
        );
    }
}