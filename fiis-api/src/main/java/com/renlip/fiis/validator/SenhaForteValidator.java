package com.renlip.fiis.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador da política mínima de senha do sistema.
 *
 * <p>Regras (MVP):
 * <ul>
 *   <li>Pelo menos 8 caracteres;</li>
 *   <li>Pelo menos uma letra (A-Z ou a-z);</li>
 *   <li>Pelo menos um dígito (0-9).</li>
 * </ul>
 *
 * <p>Intencionalmente permissivo quanto a maiúsculas/minúsculas e caracteres
 * especiais — a ideia é bloquear senhas obviamente fracas sem frustrar o
 * usuário no signup. Regras mais rígidas podem ser introduzidas depois.</p>
 *
 * @see SenhaForte
 */
public class SenhaForteValidator implements ConstraintValidator<SenhaForte, String> {

    private static final int TAMANHO_MINIMO = 8;

    @Override
    public boolean isValid(final String senha, final ConstraintValidatorContext context) {
        if (senha == null) {
            return true;
        }
        if (senha.length() < TAMANHO_MINIMO) {
            return false;
        }
        boolean temLetra = false;
        boolean temDigito = false;
        for (int i = 0; i < senha.length(); i++) {
            char c = senha.charAt(i);
            if (Character.isLetter(c)) {
                temLetra = true;
            } else if (Character.isDigit(c)) {
                temDigito = true;
            }
            if (temLetra && temDigito) {
                return true;
            }
        }
        return false;
    }
}
