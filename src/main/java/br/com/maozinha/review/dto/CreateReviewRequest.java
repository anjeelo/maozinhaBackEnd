package br.com.maozinha.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(

        @Min(
                value = 1,
                message = "A nota deve ser no mínimo 1."
        )
        @Max(
                value = 5,
                message = "A nota deve ser no máximo 5."
        )
        Short rating,

        @Size(
                max = 500,
                message = "O comentário não pode ultrapassar 500 caracteres."
        )
        String comment,

        Long reviewedUserId

) {
}