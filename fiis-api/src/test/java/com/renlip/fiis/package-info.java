/**
 * O pacote raiz {@code com.renlip.fiis} dos testes contém a classe
 * {@code FiisApiApplicationTests}, um teste de sanity check que apenas carrega o
 * contexto Spring inteiro via {@code @SpringBootTest}. Se esse teste falhar, significa
 * que alguma configuração (bean, datasource, security) está quebrada e a aplicação
 * não sobe — antes mesmo de qualquer teste funcional rodar.
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing.spring-boot-applications">Spring Boot | Testing</a>
 */
package com.renlip.fiis;
