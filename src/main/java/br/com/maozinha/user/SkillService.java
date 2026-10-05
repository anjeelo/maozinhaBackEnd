package br.com.maozinha.user;

import br.com.maozinha.common.exception.ConflictException;
import br.com.maozinha.common.exception.NotFoundException;
import br.com.maozinha.user.dto.CreateSkillRequest;
import br.com.maozinha.user.dto.SkillResponse;
import br.com.maozinha.user.dto.UserProfileResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    public SkillService(
            SkillRepository skillRepository,
            UserRepository userRepository
    ) {
        this.skillRepository = Objects.requireNonNull(
                skillRepository,
                "skillRepository não pode ser nulo"
        );

        this.userRepository = Objects.requireNonNull(
                userRepository,
                "userRepository não pode ser nulo"
        );
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> findAll() {
        return skillRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(SkillResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SkillResponse findById(Long skillId) {
        Skill skill = findSkillById(skillId);

        return SkillResponse.from(skill);
    }

    @Transactional
    public SkillResponse create(CreateSkillRequest request) {
        Objects.requireNonNull(
                request,
                "Os dados da habilidade não podem ser nulos."
        );

        String normalizedName = normalizeSkillName(request.name());

        if (skillRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException(
                    "Já existe uma habilidade com esse nome."
            );
        }

        Skill skill = Skill.create(normalizedName);

        try {
            Skill savedSkill = skillRepository.saveAndFlush(skill);
            return SkillResponse.from(savedSkill);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException(
                    "Já existe uma habilidade com esse nome.",
                    exception
            );
        }
    }

    @Transactional
    public UserProfileResponse addSkillToAuthenticatedUser(
            String authenticatedEmail,
            Long skillId
    ) {
        User user = findActiveUserByEmail(authenticatedEmail);
        Skill skill = findSkillById(skillId);

        boolean alreadyAssigned = user.getSkills()
                .stream()
                .anyMatch(existingSkill ->
                        Objects.equals(
                                existingSkill.getId(),
                                skill.getId()
                        )
                );

        if (alreadyAssigned) {
            throw new ConflictException(
                    "A habilidade já está associada ao usuário."
            );
        }

        user.addSkill(skill);

        try {
            User savedUser = userRepository.saveAndFlush(user);
            return UserProfileResponse.from(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException(
                    "A habilidade já está associada ao usuário.",
                    exception
            );
        }
    }

    @Transactional
    public UserProfileResponse removeSkillFromAuthenticatedUser(
            String authenticatedEmail,
            Long skillId
    ) {
        User user = findActiveUserByEmail(authenticatedEmail);
        Skill skill = findSkillById(skillId);

        boolean assigned = user.getSkills()
                .stream()
                .anyMatch(existingSkill ->
                        Objects.equals(
                                existingSkill.getId(),
                                skill.getId()
                        )
                );

        if (!assigned) {
            throw new NotFoundException(
                    "Habilidade não encontrada no perfil do usuário."
            );
        }

        user.removeSkill(skill);

        User savedUser = userRepository.saveAndFlush(user);

        return UserProfileResponse.from(savedUser);
    }

    private Skill findSkillById(Long skillId) {
        if (skillId == null || skillId < 1) {
            throw new NotFoundException(
                    "Habilidade não encontrada."
            );
        }

        return skillRepository
                .findById(skillId)
                .orElseThrow(() -> new NotFoundException(
                        "Habilidade não encontrada."
                ));
    }

    private User findActiveUserByEmail(String authenticatedEmail) {
        String normalizedEmail = normalizeAuthenticatedEmail(
                authenticatedEmail
        );

        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new NotFoundException(
                        "Usuário não encontrado."
                ));

        if (!user.isActive()) {
            throw new NotFoundException(
                    "Usuário não encontrado."
            );
        }

        return user;
    }

    private String normalizeAuthenticatedEmail(
            String authenticatedEmail
    ) {
        if (authenticatedEmail == null
                || authenticatedEmail.isBlank()) {
            throw new NotFoundException(
                    "Usuário não encontrado."
            );
        }

        return authenticatedEmail
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeSkillName(String name) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "O nome da habilidade não pode ser nulo."
            );
        }

        String normalizedName = name.trim();

        if (normalizedName.length() < 2
                || normalizedName.length() > 60) {
            throw new IllegalArgumentException(
                    "O nome da habilidade deve possuir entre 2 e 60 caracteres."
            );
        }

        return normalizedName;
    }
}