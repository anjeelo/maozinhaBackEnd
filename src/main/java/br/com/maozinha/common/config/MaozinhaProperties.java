package br.com.maozinha.common.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "maozinha")
public record MaozinhaProperties(
        @Valid @NotNull Job job
) {

    public record Job(
            @Min(1) int maxSelecionados
    ) {
    }
}