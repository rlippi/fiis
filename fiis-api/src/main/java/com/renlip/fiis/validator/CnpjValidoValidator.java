package com.renlip.fiis.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador de CNPJ que verifica formato e dígitos verificadores.
 *
 * <p>Aceita entrada com ou sem máscara (apenas os 14 dígitos são considerados) e rejeita
 * CNPJs com todos os dígitos iguais (casos como {@code 00000000000000} passam nos dígitos
 * verificadores mas não representam um CNPJ real).</p>
 *
 * @see CnpjValido
 */
public class CnpjValidoValidator implements ConstraintValidator<CnpjValido, String> {

    private static final int[] PESOS_DV1 = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

    private static final int[] PESOS_DV2 = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

    @Override
    public boolean isValid(final String cnpj, final ConstraintValidatorContext context) {
        if (cnpj == null) {
            return true;
        }

        String digitos = cnpj.replaceAll("\\D", "");
        if (digitos.length() != 14 || digitos.chars().distinct().count() == 1) {
            return false;
        }

        int dv1 = calcularDigito(digitos, PESOS_DV1);
        int dv2 = calcularDigito(digitos, PESOS_DV2);

        return dv1 == Character.getNumericValue(digitos.charAt(12))
            && dv2 == Character.getNumericValue(digitos.charAt(13));
    }

    private int calcularDigito(final String digitos, final int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += Character.getNumericValue(digitos.charAt(i)) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
