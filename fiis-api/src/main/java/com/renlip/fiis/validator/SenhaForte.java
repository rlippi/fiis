package com.renlip.fiis.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Anotação para validar se uma {@link String} atende à política mínima de senha
 * do sistema: pelo menos 8 caracteres, contendo ao menos uma letra e um dígito.
 *
 * <p>Se o valor for {@code null} o validador considera válido — combine com
 * {@code @NotBlank} quando o campo for obrigatório.</p>
 */
@Documented
@Constraint(validatedBy = SenhaForteValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface SenhaForte {

    String message() default "Senha deve ter no mínimo 8 caracteres, incluindo pelo menos uma letra e um número";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
