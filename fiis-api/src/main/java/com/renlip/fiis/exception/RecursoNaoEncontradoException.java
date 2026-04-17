package com.renlip.fiis.exception;

/**
 * Lançada quando um recurso solicitado não é encontrado no banco de dados.
 *
 * <p>Mapeada para HTTP 404 (Not Found) pelo {@link GlobalExceptionHandler}.</p>
 *
 * <p>Ex: tentar buscar um Fundo com ID inexistente.</p>
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    /**
     * Cria uma exceção com a mensagem informada.
     *
     * @param mensagem descrição do recurso não encontrado
     *                 (ex: "Fundo com ID 42 não encontrado")
     */
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
