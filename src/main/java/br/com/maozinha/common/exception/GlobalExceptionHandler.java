package br.com.maozinha.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final URI VALIDATION_ERROR_TYPE =
            URI.create("https://maozinha.com.br/problemas/validacao");

    private static final URI INVALID_REQUEST_TYPE =
            URI.create("https://maozinha.com.br/problemas/requisicao-invalida");

    private static final URI AUTHENTICATION_ERROR_TYPE =
            URI.create("https://maozinha.com.br/problemas/nao-autenticado");

    private static final URI ACCESS_DENIED_TYPE =
            URI.create("https://maozinha.com.br/problemas/acesso-negado");

    private static final URI RESOURCE_NOT_FOUND_TYPE =
            URI.create("https://maozinha.com.br/problemas/recurso-nao-encontrado");

    private static final URI CONFLICT_TYPE =
            URI.create("https://maozinha.com.br/problemas/conflito");

    private static final URI RATE_LIMIT_TYPE =
            URI.create("https://maozinha.com.br/problemas/limite-de-requisicoes");

    private static final URI INTERNAL_ERROR_TYPE =
            URI.create("https://maozinha.com.br/problemas/erro-interno");

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = Objects.requireNonNull(
                clock,
                "clock não pode ser nulo"
        );
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleApiException(
            ApiException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = exception.getStatus();

        ProblemDetail problem = createProblem(
                status,
                titleFor(status),
                safeMessage(
                        exception.getMessage(),
                        defaultDetailFor(status)
                ),
                typeFor(status),
                request
        );

        return ResponseEntity
                .status(status)
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldValidationError> errors = new ArrayList<>();

        exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(fieldError -> new FieldValidationError(
                        fieldError.getField(),
                        safeMessage(
                                fieldError.getDefaultMessage(),
                                "Valor inválido."
                        )
                ))
                .forEach(errors::add);

        exception.getBindingResult()
                .getGlobalErrors()
                .stream()
                .sorted(Comparator.comparing(ObjectError::getObjectName))
                .map(objectError -> new FieldValidationError(
                        objectError.getObjectName(),
                        safeMessage(
                                objectError.getDefaultMessage(),
                                "Dados inválidos."
                        )
                ))
                .forEach(errors::add);

        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Erro de validação",
                "Um ou mais campos informados são inválidos.",
                VALIDATION_ERROR_TYPE,
                request
        );

        problem.setProperty("campos", List.copyOf(errors));

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<FieldValidationError> errors = exception
                .getConstraintViolations()
                .stream()
                .sorted(Comparator.comparing(
                        violation -> violation.getPropertyPath().toString()
                ))
                .map(this::toFieldValidationError)
                .toList();

        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Erro de validação",
                "Um ou mais parâmetros informados são inválidos.",
                VALIDATION_ERROR_TYPE,
                request
        );

        problem.setProperty("campos", errors);

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        LOGGER.debug(
                "Corpo de requisição inválido em {}",
                request.getRequestURI(),
                exception
        );

        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Requisição inválida",
                "O corpo da requisição está ausente, malformado ou contém valores inválidos.",
                INVALID_REQUEST_TYPE,
                request
        );

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente",
                "O parâmetro '" + exception.getParameterName() + "' é obrigatório.",
                INVALID_REQUEST_TYPE,
                request
        );

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Parâmetro inválido",
                "O parâmetro '" + exception.getName() + "' possui um valor inválido.",
                INVALID_REQUEST_TYPE,
                request
        );

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        LOGGER.debug(
                "Falha de autenticação em {}",
                request.getRequestURI(),
                exception
        );

        ProblemDetail problem = createProblem(
                HttpStatus.UNAUTHORIZED,
                "Não autenticado",
                "É necessário fornecer credenciais válidas para acessar este recurso.",
                AUTHENTICATION_ERROR_TYPE,
                request
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        LOGGER.debug(
                "Acesso negado em {}",
                request.getRequestURI(),
                exception
        );

        ProblemDetail problem = createProblem(
                HttpStatus.FORBIDDEN,
                "Acesso negado",
                "O usuário autenticado não possui permissão para acessar este recurso.",
                ACCESS_DENIED_TYPE,
                request
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(problem);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        LOGGER.debug(
                "Rota não encontrada em {}",
                request.getRequestURI(),
                exception
        );

        ProblemDetail problem = createProblem(
                HttpStatus.NOT_FOUND,
                "Recurso não encontrado",
                "A rota solicitada não foi encontrada.",
                RESOURCE_NOT_FOUND_TYPE,
                request
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problem);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatusCode statusCode = exception.getStatusCode();
        HttpStatus status = HttpStatus.resolve(statusCode.value());

        if (status == null) {
            LOGGER.error(
                    "Status HTTP desconhecido {} recebido em {}",
                    statusCode.value(),
                    request.getRequestURI(),
                    exception
            );

            ProblemDetail problem = createProblem(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro interno",
                    "Não foi possível processar a solicitação.",
                    INTERNAL_ERROR_TYPE,
                    request
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(problem);
        }

        if (status.is5xxServerError()) {
            LOGGER.error(
                    "Erro HTTP {} ao processar {} {}",
                    status.value(),
                    request.getMethod(),
                    request.getRequestURI(),
                    exception
            );
        }

        String detail;

        if (status.is5xxServerError()) {
            detail = "Não foi possível processar a solicitação.";
        } else {
            detail = safeMessage(
                    exception.getReason(),
                    defaultDetailFor(status)
            );
        }

        ProblemDetail problem = createProblem(
                status,
                titleFor(status),
                detail,
                typeFor(status),
                request
        );

        return ResponseEntity
                .status(status)
                .body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        LOGGER.error(
                "Erro inesperado ao processar {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        ProblemDetail problem = createProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno",
                "Não foi possível processar a solicitação.",
                INTERNAL_ERROR_TYPE,
                request
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem);
    }

    private ProblemDetail createProblem(
            HttpStatus status,
            String title,
            String detail,
            URI type,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                status,
                detail
        );

        problem.setType(type);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now(clock));

        return problem;
    }

    private FieldValidationError toFieldValidationError(
            ConstraintViolation<?> violation
    ) {
        String propertyPath = violation.getPropertyPath().toString();
        String field = extractLastPathSegment(propertyPath);

        return new FieldValidationError(
                field,
                safeMessage(
                        violation.getMessage(),
                        "Valor inválido."
                )
        );
    }

    private String extractLastPathSegment(String propertyPath) {
        int lastDot = propertyPath.lastIndexOf('.');

        if (lastDot < 0 || lastDot == propertyPath.length() - 1) {
            return propertyPath;
        }

        return propertyPath.substring(lastDot + 1);
    }

    private String safeMessage(
            String message,
            String fallback
    ) {
        if (message == null || message.isBlank()) {
            return fallback;
        }

        return message;
    }

    private String titleFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST ->
                    "Requisição inválida";

            case UNAUTHORIZED ->
                    "Não autenticado";

            case FORBIDDEN ->
                    "Acesso negado";

            case NOT_FOUND ->
                    "Recurso não encontrado";

            case CONFLICT ->
                    "Conflito";

            case TOO_MANY_REQUESTS ->
                    "Limite de requisições excedido";

            case INTERNAL_SERVER_ERROR ->
                    "Erro interno";

            default -> status.is4xxClientError()
                    ? "Erro na requisição"
                    : "Erro interno";
        };
    }

    private String defaultDetailFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST ->
                    "A solicitação contém dados inválidos.";

            case UNAUTHORIZED ->
                    "É necessário fornecer credenciais válidas para acessar este recurso.";

            case FORBIDDEN ->
                    "O usuário autenticado não possui permissão para acessar este recurso.";

            case NOT_FOUND ->
                    "O recurso solicitado não foi encontrado.";

            case CONFLICT ->
                    "A solicitação não pôde ser concluída devido a um conflito.";

            case TOO_MANY_REQUESTS ->
                    "O limite de requisições foi excedido. Tente novamente mais tarde.";

            case INTERNAL_SERVER_ERROR ->
                    "Não foi possível processar a solicitação.";

            default -> status.is4xxClientError()
                    ? "A solicitação não pôde ser processada."
                    : "Não foi possível processar a solicitação.";
        };
    }

    private URI typeFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST ->
                    INVALID_REQUEST_TYPE;

            case UNAUTHORIZED ->
                    AUTHENTICATION_ERROR_TYPE;

            case FORBIDDEN ->
                    ACCESS_DENIED_TYPE;

            case NOT_FOUND ->
                    RESOURCE_NOT_FOUND_TYPE;

            case CONFLICT ->
                    CONFLICT_TYPE;

            case TOO_MANY_REQUESTS ->
                    RATE_LIMIT_TYPE;

            default ->
                    INTERNAL_ERROR_TYPE;
        };
    }

    public record FieldValidationError(
            String campo,
            String mensagem
    ) {

        public FieldValidationError {
            Objects.requireNonNull(
                    campo,
                    "campo não pode ser nulo"
            );

            Objects.requireNonNull(
                    mensagem,
                    "mensagem não pode ser nula"
            );
        }
    }
}