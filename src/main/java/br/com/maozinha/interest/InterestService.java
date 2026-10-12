package br.com.maozinha.interest;

import br.com.maozinha.common.exception.ConflictException;
import br.com.maozinha.common.exception.NotFoundException;
import br.com.maozinha.interest.dto.CreateInterestResponse;
import br.com.maozinha.interest.dto.InterestResponse;
import br.com.maozinha.job.Job;
import br.com.maozinha.job.JobRepository;
import br.com.maozinha.job.JobStatus;
import br.com.maozinha.user.User;
import br.com.maozinha.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class InterestService {

    private final InterestRepository interestRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public InterestService(
            InterestRepository interestRepository,
            JobRepository jobRepository,
            UserRepository userRepository
    ) {

        this.interestRepository = Objects.requireNonNull(
                interestRepository,
                "interestRepository não pode ser nulo"
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
    public CreateInterestResponse createInterest(
            Long jobId,
            String authenticatedEmail
    ) {

        User user =
                findActiveUserByEmail(
                        authenticatedEmail
                );

        Job job =
                findJobById(
                        jobId
                );

        if (job.getStatus() != JobStatus.ABERTO) {

            throw new ConflictException(
                    "O serviço não está disponível para receber interessados."
            );
        }

        if (job.getRequester()
                .getId()
                .equals(user.getId())) {

            throw new ConflictException(
                    "O usuário não pode demonstrar interesse no próprio serviço."
            );
        }

        if (interestRepository.existsByUserAndJob(
                user,
                job
        )) {

            throw new ConflictException(
                    "O usuário já demonstrou interesse neste serviço."
            );
        }

        Interest interest =
                Interest.create(
                        user,
                        job
                );

        Interest savedInterest =
                interestRepository.saveAndFlush(
                        interest
                );

        return CreateInterestResponse.from(
                savedInterest
        );
    }

    @Transactional
    public void withdrawInterest(
            Long interestId,
            String authenticatedEmail
    ) {

        Interest interest =
                findInterestById(
                        interestId
                );

        User user =
                findActiveUserByEmail(
                        authenticatedEmail
                );

        if (!interest.getUser()
                .getId()
                .equals(user.getId())) {

            throw new ConflictException(
                    "O usuário não possui permissão para alterar este interesse."
            );
        }

        interest.withdraw();

        interestRepository.save(
                interest
        );
    }

    @Transactional(readOnly = true)
    public List<InterestResponse> findMyInterests(
            String authenticatedEmail
    ) {

        User user =
                findActiveUserByEmail(
                        authenticatedEmail
                );

        return interestRepository
                .findByUser(user)
                .stream()
                .map(InterestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InterestResponse> findByJob(
            Long jobId
    ) {

        Job job =
                findJobById(
                        jobId
                );

        return interestRepository
                .findByJob(job)
                .stream()
                .map(InterestResponse::from)
                .toList();
    }

    private User findActiveUserByEmail(
            String email
    ) {

        String normalizedEmail =
                email.trim()
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

    private Interest findInterestById(
            Long interestId
    ) {

        return interestRepository
                .findById(interestId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Interesse não encontrado."
                        )
                );
    }
}