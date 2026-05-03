package com.renlip.fiis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Configurações do rate limiting dos endpoints públicos de autenticação.
 *
 * <p>Ligadas ao prefixo {@code fiis.rate-limit} em {@code application.yml}.
 * Cada chave define a capacidade do bucket (número máximo de requisições)
 * e o período em que esse número se recompõe.</p>
 *
 * <p><b>Escolha de chave por endpoint:</b>
 * <ul>
 *   <li>{@code login}: por e-mail — protege contra brute force em uma conta específica;</li>
 *   <li>{@code signup}: por IP — evita criação massiva de contas por bots;</li>
 *   <li>{@code forgotPassword}: por e-mail — evita flood de emails de reset para a mesma conta.</li>
 * </ul>
 *
 * <p>Storage in-memory é suficiente para o deploy atual (1 instância no Render
 * free). Se houver escala horizontal no futuro, trocar pela variante distribuída
 * do Bucket4j (Hazelcast/Redis) sem alterar o código dos services.</p>
 */
@ConfigurationProperties(prefix = "fiis.rate-limit")
public record RateLimitProperties(

    @NotNull Limite login,
    @NotNull Limite signup,
    @NotNull Limite forgotPassword
) {

    /**
     * Limite configurável para um endpoint.
     *
     * @param capacidade     número máximo de requisições permitidas na janela
     * @param duracaoMinutos duração da janela em minutos (tempo até o bucket recompor)
     */
    public record Limite(
        @Min(1) int capacidade,
        @Min(1) long duracaoMinutos
    ) {
    }
}
