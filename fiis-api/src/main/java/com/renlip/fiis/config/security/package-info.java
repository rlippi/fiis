/**
 * O pacote {@code com.renlip.fiis.config.security} centraliza a configuração de segurança
 * da aplicação usando Spring Security com autenticação JWT stateless. Agrupa o
 * {@code SecurityFilterChain}, o filtro que valida o header {@code Authorization: Bearer},
 * os {@code AuthenticationEntryPoint} e {@code AccessDeniedHandler} que padronizam respostas
 * 401/403, além do runner que realiza o seed inicial do usuário administrador.
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://docs.spring.io/spring-security/reference/servlet/architecture.html">Spring Security | Architecture</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7519">RFC 7519 | JSON Web Token (JWT)</a>
 */
package com.renlip.fiis.config.security;
