package com.renlip.fiis.exception;

import com.renlip.fiis.domain.enumeration.MensagemEnum;

import lombok.Getter;

/**
 * Lançada quando uma regra de negócio é violada.
 *
 * <p>Mapeada para HTTP 409 (Conflict) pelo {@link GlobalExceptionHandler}.</p>
 *
 * <p>Ex: tentar cadastrar um Fundo com ticker que já existe.</p>
 */
@Getter
public class RegraNegocioException extends RuntimeException {

    private final MensagemEnum mensagem;

    private final Object[] args;

    public RegraNegocioException(final MensagemEnum mensagem, final Object... args) {
        super(mensagem.getTexto());
        this.mensagem = mensagem;
        this.args = args;
    }
}
