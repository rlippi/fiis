/**
 * O pacote {@code com.renlip.fiis.controller} contém os endpoints REST da aplicação.
 * As classes aqui são anotadas com {@code @RestController}, recebem requisições HTTP,
 * validam os payloads de entrada via Bean Validation e delegam a regra de negócio para
 * a camada de serviço. As respostas são devolvidas em {@code ResponseEntity} com o
 * status HTTP apropriado.
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://docs.spring.io/spring-framework/reference/web/webmvc.html">Spring Framework | Web MVC</a>
 * @see <a href="https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann.html">Spring Framework | @RestController</a>
 */
package com.renlip.fiis.controller;
