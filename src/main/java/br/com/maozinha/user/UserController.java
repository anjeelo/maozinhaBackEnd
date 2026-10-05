package br.com.maozinha.user;

import br.com.maozinha.common.exception.NotFoundException;
import br.com.maozinha.user.dto.UpdateProfileRequest;
import br.com.maozinha.user.dto.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = Objects.requireNonNull(
                userService,
                "userService não pode ser nulo"
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            Authentication authentication
    ) {
        String authenticatedEmail = getAuthenticatedEmail(
                authentication
        );

        UserProfileResponse response =
                userService.getAuthenticatedUserProfile(
                        authenticatedEmail
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        String authenticatedEmail = getAuthenticatedEmail(
                authentication
        );

        UserProfileResponse response =
                userService.updateAuthenticatedUserProfile(
                        authenticatedEmail,
                        request
                );

        return ResponseEntity.ok(response);
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