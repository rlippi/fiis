/**
 * O pacote {@code com.renlip.fiis.domain.vo} contém os Value Objects de entrada (Request)
 * da API, representando os payloads que os endpoints recebem do consumidor. São
 * implementados como {@code record} imutáveis com anotações de Bean Validation
 * ({@code @NotBlank}, {@code @Pattern}, {@code @DecimalMin}, etc.) que validam o conteúdo
 * antes da regra de negócio rodar.
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://martinfowler.com/bliki/ValueObject.html">Martin Fowler | ValueObject</a>
 * @see <a href="https://jakarta.ee/specifications/bean-validation/3.0/">Jakarta Bean Validation 3.0</a>
 */
package com.renlip.fiis.domain.vo;
