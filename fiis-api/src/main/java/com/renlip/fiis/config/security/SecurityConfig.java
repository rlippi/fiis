package com.renlip.fiis.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Configuração central de segurança da aplicação.
 *
 * <p>Define os beans fundamentais da autenticação JWT:</p>
 * <ul>
 *   <li>{@link PasswordEncoder} — hash BCrypt para senhas;</li>
 *   <li>{@link SecurityFilterChain} — políticas de rotas e adição do filter JWT;</li>
 *   <li>{@link AuthenticationManager} — usado pelo controller de login para validar credenciais.</li>
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] ROTAS_PUBLICAS = {
        "/auth/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/actuator/health",
        "/error"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * Encoder de senhas (BCrypt) — salt automático por senha, custo 10 rounds por default.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expõe o {@link AuthenticationManager} padrão do Spring Security como bean, para que o
     * {@code AutenticacaoService} possa usá-lo ao validar credenciais no login.
     */
    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Cadeia de filtros de segurança da API.
     *
     * <p>Configuração stateless (sem sessão HTTP), CSRF desabilitado (padrão para APIs REST
     * que não usam cookies), rotas públicas liberadas e o {@link JwtAuthenticationFilter}
     * injetado antes do filter padrão de autenticação por usuário/senha.</p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(ROTAS_PUBLICAS).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
