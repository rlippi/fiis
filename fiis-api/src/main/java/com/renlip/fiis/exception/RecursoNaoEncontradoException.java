package com.renlip.fiis.exception;

import com.renlip.fiis.domain.enumeration.MensagemEnum;

import lombok.Getter;

/**
 * Lançada quando um recurso solicitado não é encontrado no banco de dados.
 *
 * <p>Mapeada para HTTP 404 (Not Found) pelo {@link GlobalExceptionHandler}.</p>
 *
 * <p>Ex: tentar buscar um Fundo com ID inexistente.</p>
 */
@Getter
public class RecursoNaoEncontradoException extends RuntimeException {

    private final MensagemEnum mensagem;

    private final Object[] args;

    public RecursoNaoEncontradoException(final MensagemEnum mensagem, final Object... args) {
        super(mensagem.getTexto());
        this.mensagem = mensagem;
        this.args = args;
    }
}
