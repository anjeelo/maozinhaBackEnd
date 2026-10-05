package br.com.maozinha.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        CorsProperties.class,
        RateLimitProperties.class,
        MaozinhaProperties.class
})
public class PropertiesConfig {
}