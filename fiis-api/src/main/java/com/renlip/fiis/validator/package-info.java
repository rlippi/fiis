/**
 * O pacote {@code com.renlip.fiis.validator} contém validadores customizados do Bean
 * Validation — anotações próprias da aplicação (ex: {@code @CnpjValido}) acompanhadas
 * de suas implementações de {@code ConstraintValidator}. O mecanismo permite aplicar
 * validações específicas do domínio nos VOs de entrada com a mesma ergonomia das
 * anotações padrão ({@code @NotBlank}, {@code @Size}, etc.).
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://jakarta.ee/specifications/bean-validation/3.0/">Jakarta Bean Validation 3.0</a>
 */
package com.renlip.fiis.validator;
