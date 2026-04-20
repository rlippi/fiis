/**
 * O pacote {@code com.renlip.fiis.exception} contém as exceções customizadas da aplicação
 * (ex: {@code RecursoNaoEncontradoException}, {@code RegraNegocioException}) e o
 * {@code GlobalExceptionHandler} anotado com {@code @RestControllerAdvice}, que as
 * intercepta e converte em respostas HTTP padronizadas no formato {@code ErroResponse}
 * (com campo {@code codigo} estável para o cliente).
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-advice.html">Spring Framework | @ControllerAdvice</a>
 */
package com.renlip.fiis.exception;
