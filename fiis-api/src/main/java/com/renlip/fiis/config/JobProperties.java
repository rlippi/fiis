package com.renlip.fiis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Configurações dos jobs agendados da aplicação.
 *
 * <p>Ligadas ao prefixo {@code fiis.job} em {@code application.yml}. Cada sub-record
 * representa um job distinto, permitindo que novos jobs sejam adicionados sem
 * reestruturar as propriedades existentes.</p>
 *
 * <p><b>Por que sub-records em vez de classes separadas?</b> Spring Boot suporta
 * {@code @ConfigurationProperties} com records aninhados, o que mantém toda a
 * configuração de "jobs" em um único ponto no YAML e uma única classe Java.</p>
 */
@ConfigurationProperties(prefix = "fiis.job")
public record JobProperties(

    @NotNull AtualizarCotacoes atualizarCotacoes
) {

    /**
     * Configuração do job de atualização diária de cotações via BRAPI.
     *
     * @param enabled se {@code true}, o agendamento dispara conforme {@link #cron};
     *                se {@code false}, o método {@code agendar()} sai sem executar
     *                (útil para pausar o job sem redeploy via variável de ambiente).
     *                O endpoint manual em {@code /api/jobs/atualizar-cotacoes} continua
     *                funcionando independentemente deste flag.
     * @param cron    expressão cron de 6 campos (seg min hora dia mês diaSemana) que
     *                define quando o agendamento dispara. Ex: {@code "0 0 19 * * MON-FRI"}
     *                = toda hora 19:00 de segunda a sexta.
     */
    public record AtualizarCotacoes(
        boolean enabled,
        @NotBlank String cron
    ) {
    }
}
