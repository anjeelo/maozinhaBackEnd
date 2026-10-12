package br.com.maozinha.admin;

import br.com.maozinha.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@Table(
        name = "acao_moderacao",
        schema = "public"
)
public class ModerationAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "administrador_id",
            nullable = false,
            updatable = false
    )
    private User administrator;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo_alvo",
            nullable = false,
            length = 10,
            updatable = false
    )
    private ModerationTargetType targetType;

    @Column(
            name = "alvo_id",
            nullable = false,
            updatable = false
    )
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "acao",
            nullable = false,
            length = 30,
            updatable = false
    )
    private ModerationActionType action;

    @Column(
            name = "motivo",
            nullable = false,
            length = 500,
            updatable = false
    )
    private String reason;

    @Column(
            name = "data_acao",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime actionDate;

    protected ModerationAction() {
    }

    private ModerationAction(
            User administrator,
            ModerationTargetType targetType,
            Long targetId,
            ModerationActionType action,
            String reason
    ) {
        this.administrator = validateAdministrator(
                administrator
        );

        this.targetType = Objects.requireNonNull(
                targetType,
                "O tipo de alvo não pode ser nulo."
        );

        this.targetId = validateTargetId(
                targetId
        );

        this.action = validateAction(
                targetType,
                action
        );

        this.reason = validateReason(
                reason
        );
    }

    public static ModerationAction create(
            User administrator,
            ModerationTargetType targetType,
            Long targetId,
            ModerationActionType action,
            String reason
    ) {
        return new ModerationAction(
                administrator,
                targetType,
                targetId,
                action,
                reason
        );
    }

    @PrePersist
    private void prePersist() {
        if (actionDate == null) {
            actionDate = OffsetDateTime.now(
                    ZoneOffset.UTC
            );
        }
    }

    private static User validateAdministrator(
            User administrator
    ) {
        Objects.requireNonNull(
                administrator,
                "O administrador não pode ser nulo."
        );

        if (!administrator.isActive()
                || !administrator.isAdmin()) {

            throw new IllegalArgumentException(
                    "A ação deve ser registrada por um administrador ativo."
            );
        }

        return administrator;
    }

    private static Long validateTargetId(
            Long targetId
    ) {
        if (targetId == null || targetId <= 0) {
            throw new IllegalArgumentException(
                    "O identificador do alvo deve ser maior que zero."
            );
        }

        return targetId;
    }

    private static ModerationActionType validateAction(
            ModerationTargetType targetType,
            ModerationActionType action
    ) {
        Objects.requireNonNull(
                action,
                "A ação não pode ser nula."
        );

        if (!action.supports(targetType)) {
            throw new IllegalArgumentException(
                    "A ação informada não é válida para o tipo de alvo."
            );
        }

        return action;
    }

    private static String validateReason(
            String reason
    ) {
        Objects.requireNonNull(
                reason,
                "O motivo não pode ser nulo."
        );

        String normalizedReason = reason.trim();

        int length = normalizedReason.codePointCount(
                0,
                normalizedReason.length()
        );

        if (length < 5 || length > 500) {
            throw new IllegalArgumentException(
                    "O motivo deve possuir entre 5 e 500 caracteres."
            );
        }

        return normalizedReason;
    }

    public Long getId() {
        return id;
    }

    public User getAdministrator() {
        return administrator;
    }

    public ModerationTargetType getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public ModerationActionType getAction() {
        return action;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getActionDate() {
        return actionDate;
    }

    @Override
    public boolean equals(
            Object object
    ) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof ModerationAction other)) {
            return false;
        }

        return id != null
                && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}