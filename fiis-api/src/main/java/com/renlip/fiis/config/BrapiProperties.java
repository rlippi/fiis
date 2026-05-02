package com.renlip.fiis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Configurações de integração com a BRAPI (https://brapi.dev) para obter
 * cotações de FIIs a partir do mercado.
 *
 * <p>Ligadas ao prefixo {@code brapi} em {@code application.yml}. O token é
 * opcional: o free tier da BRAPI não exige autenticação. Se um token for
 * fornecido (via env var {@code BRAPI_TOKEN}), ele é enviado na query string
 * para que o rate limit do plano pago seja usado.</p>
 *
 * <p>Uso de {@code record} como idioma moderno para beans de configuração
 * imutáveis em Java 21.</p>
 */
@ConfigurationProperties(prefix = "brapi")
public record BrapiProperties(

    /**
     * URL base da BRAPI (ex: {@code https://brapi.dev}).
     */
    @NotBlank String url,

    /**
     * Token opcional para autenticação no plano pago. Pode ser nulo ou vazio.
     */
    String token,

    /**
     * Timeout em milissegundos aplicado tanto ao connect quanto ao read da
     * chamada HTTP à BRAPI. Usado pelo {@code RestClient} construído no
     * {@code BrapiTickerFetcher}. Default razoável: 5000ms — BRAPI normalmente
     * responde em menos de 1s, então 5s já é folga suficiente para variações.
     */
    @Min(100) int timeoutMillis
) {
}
