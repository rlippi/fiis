package com.renlip.fiis.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Anotação para validar se uma {@link String} representa um CNPJ válido.
 *
 * <p>A validação verifica tanto o formato (14 dígitos, com ou sem máscara) quanto os dígitos
 * verificadores conforme o algoritmo oficial da Receita Federal.</p>
 *
 * <p>Se o valor associado ao atributo com esta anotação for {@code null}, será considerado
 * válido, permitindo seu uso em conjunto com {@code @NotNull} quando o campo for obrigatório.</p>
 */
@Documented
@Constraint(validatedBy = CnpjValidoValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface CnpjValido {

    String message() default "CNPJ inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
