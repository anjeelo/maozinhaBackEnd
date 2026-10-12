package br.com.maozinha.admin;

public enum ModerationActionType {

    SUSPENDER_USUARIO(
            ModerationTargetType.USUARIO
    ),

    REATIVAR_USUARIO(
            ModerationTargetType.USUARIO
    ),

    EXCLUIR_USUARIO(
            ModerationTargetType.USUARIO
    ),

    OCULTAR_SERVICO(
            ModerationTargetType.SERVICO
    ),

    RESTAURAR_SERVICO(
            ModerationTargetType.SERVICO
    ),

    REMOVER_AVALIACAO(
            ModerationTargetType.AVALIACAO
    ),

    RESTAURAR_AVALIACAO(
            ModerationTargetType.AVALIACAO
    );

    private final ModerationTargetType targetType;

    ModerationActionType(
            ModerationTargetType targetType
    ) {
        this.targetType = targetType;
    }

    public ModerationTargetType getTargetType() {
        return targetType;
    }

    public boolean supports(
            ModerationTargetType targetType
    ) {
        return this.targetType == targetType;
    }
}