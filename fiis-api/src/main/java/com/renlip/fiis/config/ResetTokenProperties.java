package com.renlip.fiis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.Min;

/**
 * Configurações do token de reset de senha.
 *
 * <p>Ligadas ao prefixo {@code fiis.reset-token} em {@code application.yml}.</p>
 *
 * @param ttlMinutos período em minutos durante o qual o token emitido aceita ser consumido
 */
@ConfigurationProperties(prefix = "fiis.reset-token")
public record ResetTokenProperties(

    @Min(1) long ttlMinutos
) {
}
