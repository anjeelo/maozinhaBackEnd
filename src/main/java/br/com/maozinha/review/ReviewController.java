package br.com.maozinha.review;

import br.com.maozinha.common.exception.NotFoundException;
import br.com.maozinha.review.dto.CreateReviewRequest;
import br.com.maozinha.review.dto.ReviewResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(
            ReviewService reviewService
    ) {

        this.reviewService = Objects.requireNonNull(
                reviewService,
                "reviewService não pode ser nulo"
        );
    }

    @PostMapping("/api/services/{id}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            Authentication authentication,

            @PathVariable("id")
            @Positive
            Long jobId,

            @Valid
            @RequestBody
            CreateReviewRequest request
    ) {

        String authenticatedEmail =
                getAuthenticatedEmail(
                        authentication
                );

        ReviewResponse response =
                reviewService.createReview(
                        jobId,
                        authenticatedEmail,
                        request
                );

        URI location = URI.create(
                "/api/services/"
                        + jobId
                        + "/reviews/"
                        + response.id()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(location)
                .body(response);
    }

    @GetMapping("/api/users/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> findReviewsByUser(
            @PathVariable("id")
            @Positive
            Long userId
    ) {

        return ResponseEntity.ok(
                reviewService.findReviewsByUser(
                        userId
                )
        );
    }

    private String getAuthenticatedEmail(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new NotFoundException(
                    "Usuário não encontrado."
            );
        }

        return authentication.getName();
    }
}