package br.com.maozinha.job;

import br.com.maozinha.common.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;

@Component
public class JobStateMachine {

    private static final Map<JobStatus, EnumSet<JobStatus>> ALLOWED_TRANSITIONS =
            Map.of(
                    JobStatus.ABERTO,
                    EnumSet.of(
                            JobStatus.SELECIONADO,
                            JobStatus.CANCELADO
                    ),

                    JobStatus.SELECIONADO,
                    EnumSet.of(
                            JobStatus.EM_ANDAMENTO,
                            JobStatus.CANCELADO
                    ),

                    JobStatus.EM_ANDAMENTO,
                    EnumSet.of(
                            JobStatus.CONCLUIDO,
                            JobStatus.CANCELADO
                    ),

                    JobStatus.CONCLUIDO,
                    EnumSet.noneOf(
                            JobStatus.class
                    ),

                    JobStatus.CANCELADO,
                    EnumSet.noneOf(
                            JobStatus.class
                    )
            );

    public void validateTransition(
            JobStatus currentStatus,
            JobStatus targetStatus
    ) {

        if (currentStatus == null) {

            throw new IllegalArgumentException(
                    "O status atual não pode ser nulo."
            );
        }

        if (targetStatus == null) {

            throw new IllegalArgumentException(
                    "O status de destino não pode ser nulo."
            );
        }

        if (currentStatus == targetStatus) {
            return;
        }

        EnumSet<JobStatus> allowedStatuses =
                ALLOWED_TRANSITIONS.get(
                        currentStatus
                );

        if (allowedStatuses == null
                || !allowedStatuses.contains(
                targetStatus
        )) {

            throw new InvalidStateTransitionException(
                    "Serviço",
                    currentStatus.name(),
                    targetStatus.name()
            );
        }
    }

    public boolean canTransition(
            JobStatus currentStatus,
            JobStatus targetStatus
    ) {

        if (currentStatus == null
                || targetStatus == null) {
            return false;
        }

        if (currentStatus == targetStatus) {
            return true;
        }

        EnumSet<JobStatus> allowedStatuses =
                ALLOWED_TRANSITIONS.get(
                        currentStatus
                );

        return allowedStatuses != null
                && allowedStatuses.contains(
                targetStatus
        );
    }
}