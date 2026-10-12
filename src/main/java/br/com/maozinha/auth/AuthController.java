package br.com.maozinha.auth;

import br.com.maozinha.auth.dto.ForgotPasswordRequest;
import br.com.maozinha.auth.dto.ForgotPasswordResponse;
import br.com.maozinha.auth.dto.LoginRequest;
import br.com.maozinha.auth.dto.RefreshTokenRequest;
import br.com.maozinha.auth.dto.RefreshTokenResponse;
import br.com.maozinha.auth.dto.RegisterRequest;
import br.com.maozinha.auth.dto.RegisterResponse;
import br.com.maozinha.auth.dto.ResetPasswordRequest;
import br.com.maozinha.auth.dto.ResetPasswordResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService
    ) {
        this.authService = Objects.requireNonNull(
                authService,
                "authService não pode ser nulo"
        );
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response =
                authService.register(request);

        URI location = URI.create(
                "/api/users/" + response.id()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(location)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<RefreshTokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(
                authService.refresh(request)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(
                authService.forgotPassword(request)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(
                authService.resetPassword(request)
        );
    }
}