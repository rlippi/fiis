package com.renlip.fiis.exception;

import com.renlip.fiis.domain.enumeration.MensagemEnum;

import lombok.Getter;

/**
 * Lançada quando o rate limit de um endpoint é excedido.
 *
 * <p>Mapeada para HTTP 429 (Too Many Requests) pelo {@link GlobalExceptionHandler}.</p>
 */
@Getter
public class LimiteRequisicoesException extends RuntimeException {

    private final MensagemEnum mensagem;

    private final Object[] args;

    public LimiteRequisicoesException(final MensagemEnum mensagem, final Object... args) {
        super(mensagem.getTexto());
        this.mensagem = mensagem;
        this.args = args;
    }
}
