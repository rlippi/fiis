/**
 * O pacote {@code com.renlip.fiis.audit} centraliza a auditoria do sistema:
 * listeners que reagem a eventos de domínio (definidos em
 * {@code com.renlip.fiis.domain.event}) e registram trilha estruturada das
 * mudanças relevantes.
 *
 * <p>A auditoria é tratada como <i>cross-cutting concern</i> de relevância
 * de negócio — por isso recebe pacote próprio em vez de ficar misturada
 * em {@code support/}. À medida que cresce, este pacote pode acomodar:
 * <ul>
 *   <li>{@code listener/}: listeners por domínio (hoje, tudo no
 *       {@code AuditoriaListener} central);</li>
 *   <li>{@code entity/}: entidades de auditoria persistida (ex:
 *       {@code LogAuditoria}) caso decidamos guardar em tabela;</li>
 *   <li>{@code annotation/}: marcadores como {@code @Audited} para automação;</li>
 *   <li>configuração de Hibernate Envers, se adotado.</li>
 * </ul>
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html">Spring Reference — Transactional Events</a>
 */
package com.renlip.fiis.audit;
