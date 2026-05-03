package com.renlip.fiis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;

/**
 * Configurações de envio de email transacional.
 *
 * <p>Ligadas ao prefixo {@code fiis.mail} em {@code application.yml}. Os
 * parâmetros SMTP propriamente ditos (host, porta, credenciais) continuam sob
 * {@code spring.mail}, conforme o starter oficial do Spring Boot.</p>
 *
 * @param enabled     quando {@code false}, o service apenas loga o corpo do email
 *                    em vez de enviar — útil em dev sem SMTP configurado
 * @param fromAddress endereço exibido no cabeçalho {@code From}
 * @param frontendUrl URL base do frontend, usada para montar links dentro dos emails
 *                    (ex: link de reset de senha)
 */
@ConfigurationProperties(prefix = "fiis.mail")
public record MailProperties(

    boolean enabled,

    @NotBlank String fromAddress,

    @NotBlank String frontendUrl
) {
}
