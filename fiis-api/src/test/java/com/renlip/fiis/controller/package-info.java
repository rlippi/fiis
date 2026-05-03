/**
 * O pacote {@code com.renlip.fiis.controller} dos testes contém os testes de integração
 * dos controllers REST, organizados em classes aninhadas ({@code @Nested}) por endpoint
 * e por cenário (sucesso / falha). Os testes usam {@code @SpringBootTest} com o profile
 * {@code test}, {@code MockMvc} para simular as chamadas HTTP, {@code @Sql} para preparar
 * dados e utilitários de {@code spring-security-test} para autenticar com usuários mock.
 * As respostas são comparadas contra fixtures JSON em
 * {@code src/test/resources/scenarios/}.
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html">Spring Framework | MockMvc</a>
 * @see <a href="https://docs.spring.io/spring-security/reference/servlet/test/index.html">Spring Security | Testing</a>
 */
package com.renlip.fiis.controller;
