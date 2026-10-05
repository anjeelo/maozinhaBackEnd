package br.com.maozinha.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidStateTransitionException extends ApiException {

    public InvalidStateTransitionException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public InvalidStateTransitionException(
            String entityName,
            String currentState,
            String targetState
    ) {
        super(
                HttpStatus.CONFLICT,
                String.format(
                        "Não é possível alterar o estado de %s de '%s' para '%s'.",
                        entityName,
                        currentState,
                        targetState
                )
        );
    }

    public InvalidStateTransitionException(String message, Throwable cause) {
        super(HttpStatus.CONFLICT, message, cause);
    }
}