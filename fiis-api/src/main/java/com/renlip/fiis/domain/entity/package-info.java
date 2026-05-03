/**
 * O pacote {@code com.renlip.fiis.domain.entity} contém as entidades JPA que representam
 * as tabelas do banco relacional. As classes aqui são anotadas com {@code @Entity} e
 * {@code @Table}, com relacionamentos ({@code @ManyToOne}, {@code @OneToMany}) mapeados
 * preferencialmente em modo {@code LAZY}. Tipos numéricos monetários usam
 * {@code BigDecimal} e enums são persistidos como {@code @Enumerated(STRING)}.
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html">Jakarta Persistence 3.0</a>
 * @see <a href="https://docs.jboss.org/hibernate/orm/6.4/userguide/html_single/Hibernate_User_Guide.html">Hibernate ORM 6.4 | User Guide</a>
 */
package com.renlip.fiis.domain.entity;
