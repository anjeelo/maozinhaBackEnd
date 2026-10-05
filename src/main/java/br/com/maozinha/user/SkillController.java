package br.com.maozinha.user;

import br.com.maozinha.common.exception.NotFoundException;
import br.com.maozinha.user.dto.CreateSkillRequest;
import br.com.maozinha.user.dto.SkillResponse;
import br.com.maozinha.user.dto.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = Objects.requireNonNull(
                skillService,
                "skillService não pode ser nulo"
        );
    }

    @GetMapping("/api/skills")
    public ResponseEntity<List<SkillResponse>> findAll() {
        return ResponseEntity.ok(
                skillService.findAll()
        );
    }

    @GetMapping("/api/skills/{id}")
    public ResponseEntity<SkillResponse> findById(
            @PathVariable("id") Long skillId
    ) {
        return ResponseEntity.ok(
                skillService.findById(skillId)
        );
    }

    @PostMapping("/api/skills")
    public ResponseEntity<SkillResponse> create(
            @Valid @RequestBody CreateSkillRequest request
    ) {
        SkillResponse response = skillService.create(request);

        URI location = URI.create(
                "/api/skills/" + response.id()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(location)
                .body(response);
    }

    @PostMapping("/api/users/me/skills/{id}")
    public ResponseEntity<UserProfileResponse> addToMyProfile(
            Authentication authentication,
            @PathVariable("id") Long skillId
    ) {
        String authenticatedEmail = getAuthenticatedEmail(
                authentication
        );

        UserProfileResponse response =
                skillService.addSkillToAuthenticatedUser(
                        authenticatedEmail,
                        skillId
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/users/me/skills/{id}")
    public ResponseEntity<UserProfileResponse> removeFromMyProfile(
            Authentication authentication,
            @PathVariable("id") Long skillId
    ) {
        String authenticatedEmail = getAuthenticatedEmail(
                authentication
        );

        UserProfileResponse response =
                skillService.removeSkillFromAuthenticatedUser(
                        authenticatedEmail,
                        skillId
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