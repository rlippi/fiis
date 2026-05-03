/**
 * O pacote {@code com.renlip.fiis.domain.event} reúne os <b>fatos de domínio</b>
 * publicados via {@code ApplicationEventPublisher}: ocorrências relevantes
 * para outras camadas (auditoria, métricas, integrações) sem que o emissor
 * precise conhecer seus consumidores.
 *
 * <p>Cada evento é um {@code record} imutável carregando um snapshot dos campos
 * relevantes — não a entidade JPA — para que listeners possam ser executados
 * em fases pós-commit sem risco de inconsistência ou {@code LazyInitializationException}.</p>
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events">Spring Reference — Standard and Custom Events</a>
 */
package com.renlip.fiis.domain.event;
