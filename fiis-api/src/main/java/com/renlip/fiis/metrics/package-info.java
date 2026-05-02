/**
 * O pacote {@code com.renlip.fiis.metrics} concentra a instrumentação de
 * métricas Micrometer da aplicação: listeners de eventos de domínio e
 * registradores de timers/counters/gauges customizados.
 *
 * <p>Métricas é tratado como <i>cross-cutting concern</i> com pacote próprio
 * (paralelo a {@code audit/}) — cada um tem responsabilidade única, mesmo que
 * consumam os mesmos eventos de {@code domain/event/}. As métricas registradas
 * aqui aparecem em {@code /actuator/metrics} e {@code /actuator/prometheus}.</p>
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://docs.micrometer.io/micrometer/reference/">Micrometer Reference Documentation</a>
 */
package com.renlip.fiis.metrics;
