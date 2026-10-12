package br.com.maozinha.interest;

import br.com.maozinha.job.Job;
import br.com.maozinha.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterestRepository
        extends JpaRepository<Interest, Long> {

    Optional<Interest> findByUserAndJob(
            User user,
            Job job
    );

    boolean existsByUserAndJob(
            User user,
            Job job
    );

    List<Interest> findByJob(
            Job job
    );

    List<Interest> findByUser(
            User user
    );

    List<Interest> findByJobAndStatus(
            Job job,
            InterestStatus status
    );
}