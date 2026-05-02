package com.renlip.fiis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.Min;

/**
 * Configurações do refresh token JWT.
 *
 * <p>Ligadas ao prefixo {@code fiis.refresh-token} em {@code application.yml}.</p>
 *
 * @param ttlDias tempo de vida do refresh token em dias. Default: 7.
 *                Refresh longo evita re-login frequente; access curto (15min,
 *                em {@code fiis.jwt.ttl-millis}) reduz a janela em caso de roubo.
 */
@ConfigurationProperties(prefix = "fiis.refresh-token")
public record RefreshTokenProperties(

    @Min(1) long ttlDias
) {
}
