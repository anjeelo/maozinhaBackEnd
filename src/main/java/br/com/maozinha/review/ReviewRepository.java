package br.com.maozinha.review;

import br.com.maozinha.job.Job;
import br.com.maozinha.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    Optional<Review> findByJobAndReviewerAndReviewed(
            Job job,
            User reviewer,
            User reviewed
    );

    boolean existsByJobAndReviewerAndReviewed(
            Job job,
            User reviewer,
            User reviewed
    );

    List<Review> findByReviewedOrderByReviewDateDesc(
            User reviewed
    );

    List<Review> findByReviewerOrderByReviewDateDesc(
            User reviewer
    );

    List<Review> findByJob(
            Job job
    );

    List<Review> findByReviewedAndRemovedByModerationFalseOrderByReviewDateDesc(
            User reviewed
    );
}