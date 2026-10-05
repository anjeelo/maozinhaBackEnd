package br.com.maozinha.common.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "security.rate-limit")
public record RateLimitProperties(
        @Positive long capacity,
        @Positive long refillTokens,
        @NotNull Duration refillPeriod
) {
}