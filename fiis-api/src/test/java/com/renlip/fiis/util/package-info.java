/**
 * O pacote {@code com.renlip.fiis.util} dos testes reúne utilitários compartilhados que
 * reduzem o boilerplate dos testes de integração. O {@code JsonUtils} carrega fixtures
 * JSON do classpath de teste (scenarios de input e expected output), enquanto o
 * {@code RestTestClient} encapsula o {@code MockMvc} com uma API fluente
 * ({@code post(...).expectStatus(...).expectBody(...)}) que compara respostas contra
 * fixtures via JSONAssert.
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://github.com/skyscreamer/JSONassert">JSONassert</a>
 */
package com.renlip.fiis.util;
