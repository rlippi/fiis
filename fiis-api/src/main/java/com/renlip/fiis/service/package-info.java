/**
 * O pacote {@code com.renlip.fiis.service} contém classes que encapsulam a lógica de
 * negócio da aplicação. Essas classes fornecem uma <a href="https://martinfowler.com/eaaCatalog/serviceLayer.html">camada
 * de serviço</a> para centralizar a construção da lógica de negócio, evitando a
 * duplicação de código ao lidar com diferentes tipos de interface de entrada
 * (controllers, schedulers, listeners) para os dados da aplicação. As transações são
 * demarcadas aqui via {@code @Transactional}, e as classes são tipicamente injetadas
 * por construtor ({@code @RequiredArgsConstructor}).
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://martinfowler.com/eaaCatalog/serviceLayer.html">Martin Fowler | Service Layer</a>
 * @see <a href="https://docs.spring.io/spring-framework/reference/data-access/transaction.html">Spring Framework | Transaction Management</a>
 */
package com.renlip.fiis.service;
