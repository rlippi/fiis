package com.renlip.fiis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita o processamento de {@code @Scheduled} em todos os profiles <b>exceto</b>
 * {@code test}.
 *
 * <p>Manter o agendador desligado em testes evita que jobs disparem durante a
 * execução da suíte, onde horas relógio e estado do banco são imprevisíveis.
 * Como as annotations {@code @Scheduled} são metadados inertes sem
 * {@code @EnableScheduling}, os beans continuam sendo criados normalmente e
 * seus métodos podem ser invocados de forma direta (por testes unitários ou
 * pelo endpoint manual {@code /api/jobs/*}).</p>
 */
@Configuration
@Profile("!test")
@EnableScheduling
public class SchedulingConfig {
}
