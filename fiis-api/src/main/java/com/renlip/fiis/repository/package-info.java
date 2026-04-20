/**
 * O pacote {@code com.renlip.fiis.repository} contém as interfaces Spring Data JPA
 * responsáveis pela persistência da aplicação. As interfaces estendem
 * {@code JpaRepository} e combinam derived queries (nomeadas automaticamente, ex:
 * {@code findByEmailAndAtivoTrue}) com {@code @Query} JPQL quando a consulta exige
 * mais flexibilidade.
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://docs.spring.io/spring-data/jpa/reference/jpa.html">Spring Data JPA | Reference</a>
 */
package com.renlip.fiis.repository;
