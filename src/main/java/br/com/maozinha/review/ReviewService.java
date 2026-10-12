package br.com.maozinha.review;

import br.com.maozinha.common.exception.ConflictException;
import br.com.maozinha.common.exception.NotFoundException;
import br.com.maozinha.job.Job;
import br.com.maozinha.job.JobRepository;
import br.com.maozinha.job.JobStatus;
import br.com.maozinha.review.dto.CreateReviewRequest;
import br.com.maozinha.review.dto.ReviewResponse;
import br.com.maozinha.user.User;
import br.com.maozinha.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            JobRepository jobRepository,
            UserRepository userRepository
    ) {

        this.reviewRepository = Objects.requireNonNull(
                reviewRepository,
                "reviewRepository não pode ser nulo"
        );

        this.jobRepository = Objects.requireNonNull(
                jobRepository,
                "jobRepository não pode ser nulo"
        );

        this.userRepository = Objects.requireNonNull(
                userRepository,
                "userRepository não pode ser nulo"
        );
    }

    @Transactional
    public ReviewResponse createReview(
            Long jobId,
            String authenticatedEmail,
            CreateReviewRequest request
    ) {

        User reviewer =
                findActiveUserByEmail(
                        authenticatedEmail
                );

        User reviewed =
                findUserById(
                        request.reviewedUserId()
                );

        Job job =
                findJobById(
                        jobId
                );

        if (job.getStatus() != JobStatus.CONCLUIDO) {

            throw new ConflictException(
                    "Somente serviços concluídos podem receber avaliações."
            );
        }

        if (reviewRepository.existsByJobAndReviewerAndReviewed(
                job,
                reviewer,
                reviewed
        )) {

            throw new ConflictException(
                    "A avaliação já foi registrada."
            );
        }

        Review review =
                Review.create(
                        request.rating(),
                        request.comment(),
                        reviewer,
                        reviewed,
                        job
                );

        Review savedReview =
                reviewRepository.saveAndFlush(
                        review
                );

        return ReviewResponse.from(
                savedReview
        );
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> findReviewsByUser(
            Long userId
    ) {

        User reviewed =
                findUserById(
                        userId
                );

        return reviewRepository
                .findByReviewedAndRemovedByModerationFalseOrderByReviewDateDesc(
                        reviewed
                )
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    private User findUserById(
            Long userId
    ) {

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Usuário não encontrado."
                        )
                );
    }

    private User findActiveUserByEmail(
            String email
    ) {

        String normalizedEmail =
                email
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        User user =
                userRepository
                        .findByEmailIgnoreCase(
                                normalizedEmail
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Usuário não encontrado."
                                )
                        );

        if (!user.isActive()) {

            throw new NotFoundException(
                    "Usuário não encontrado."
            );
        }

        return user;
    }

    private Job findJobById(
            Long jobId
    ) {

        return jobRepository
                .findById(jobId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Serviço não encontrado."
                        )
                );
    }
}