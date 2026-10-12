package br.com.maozinha.admin;

import br.com.maozinha.common.exception.ForbiddenException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(
            AdminService adminService
    ) {
        this.adminService = Objects.requireNonNull(
                adminService,
                "adminService não pode ser nulo"
        );
    }

    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<Void> suspendUser(
            @PathVariable("id")
            @Positive(
                    message = "O identificador do usuário deve ser maior que zero."
            )
            Long userId,

            @Valid
            @RequestBody
            ModerationReasonRequest request,

            Authentication authentication
    ) {
        String authenticatedEmail = getAuthenticatedEmail(
                authentication
        );

        adminService.suspendUser(
                userId,
                authenticatedEmail,
                request.reason()
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/reactivate")
    public ResponseEntity<Void> reactivateUser(
            @PathVariable("id")
            @Positive(
                    message = "O identificador do usuário deve ser maior que zero."
            )
            Long userId,

            @Valid
            @RequestBody
            ModerationReasonRequest request,

            Authentication authentication
    ) {
        String authenticatedEmail = getAuthenticatedEmail(
                authentication
        );

        adminService.reactivateUser(
                userId,
                authenticatedEmail,
                request.reason()
        );

        return ResponseEntity.noContent().build();
    }

    private String getAuthenticatedEmail(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new ForbiddenException(
                    "É necessário autenticar-se como administrador."
            );
        }

        return authentication.getName();
    }

    public record ModerationReasonRequest(

            @NotBlank(
                    message = "O motivo da ação administrativa é obrigatório."
            )
            @Size(
                    min = 5,
                    max = 500,
                    message = "O motivo deve possuir entre 5 e 500 caracteres."
            )
            String reason

    ) {
    }
}