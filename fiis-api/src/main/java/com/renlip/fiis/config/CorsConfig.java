package com.renlip.fiis.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuração de CORS (Cross-Origin Resource Sharing) da API.
 *
 * <p>Libera chamadas vindas de:</p>
 * <ul>
 *   <li>{@code http://localhost:4200} e {@code http://localhost:4201} — Angular dev server;</li>
 *   <li>{@code https://*.vercel.app} — deploys de preview e produção do frontend no Vercel.</li>
 * </ul>
 *
 * <p>O bean {@link CorsConfigurationSource} é detectado automaticamente pelo Spring Security
 * quando {@code http.cors(Customizer.withDefaults())} é chamado no {@code SecurityConfig}.</p>
 */
@Configuration
public class CorsConfig {

    private static final List<String> ORIGINS_PERMITIDAS = List.of(
        "http://localhost:4200",
        "http://localhost:4201"
    );

    private static final List<String> ORIGIN_PATTERNS_PERMITIDAS = List.of(
        "https://*.vercel.app"
    );

    private static final List<String> METODOS_PERMITIDOS = List.of(
        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    );

    private static final long MAX_AGE_SEGUNDOS = 3600L;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ORIGINS_PERMITIDAS);
        config.setAllowedOriginPatterns(ORIGIN_PATTERNS_PERMITIDAS);
        config.setAllowedMethods(METODOS_PERMITIDOS);
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(MAX_AGE_SEGUNDOS);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
