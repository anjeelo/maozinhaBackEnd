package br.com.maozinha.user.dto;

import br.com.maozinha.user.Role;
import br.com.maozinha.user.Skill;
import br.com.maozinha.user.User;
import br.com.maozinha.user.UserStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

public record UserProfileResponse(

        Long id,
        String name,
        String whatsapp,
        String city,
        String neighborhood,
        Role role,
        UserStatus status,
        BigDecimal averageRating,
        Integer totalReviews,
        List<String> skills,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public UserProfileResponse {
        skills = skills == null
                ? List.of()
                : List.copyOf(skills);
    }

    public static UserProfileResponse from(User user) {
        List<String> skillNames = user.getSkills()
                .stream()
                .map(Skill::getName)
                .sorted(Comparator.naturalOrder())
                .toList();

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getWhatsapp(),
                user.getCity(),
                user.getNeighborhood(),
                user.getRole(),
                user.getStatus(),
                user.getAverageRating(),
                user.getTotalReviews(),
                skillNames,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}