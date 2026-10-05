package br.com.maozinha.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public NotFoundException(String resourceName, Object resourceId) {
        super(
                HttpStatus.NOT_FOUND,
                String.format("%s com identificador '%s' não foi encontrado.", resourceName, resourceId)
        );
    }

    public NotFoundException(String message, Throwable cause) {
        super(HttpStatus.NOT_FOUND, message, cause);
    }
}