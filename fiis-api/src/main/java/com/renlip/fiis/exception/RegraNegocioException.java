package com.renlip.fiis.exception;

/**
 * Lançada quando uma regra de negócio é violada.
 *
 * <p>Mapeada para HTTP 409 (Conflict) pelo {@link GlobalExceptionHandler}.</p>
 *
 * <p>Ex: tentar cadastrar um Fundo com ticker que já existe.</p>
 */
public class RegraNegocioException extends RuntimeException {

    /**
     * Cria uma exceção com a mensagem informada.
     *
     * @param mensagem descrição da regra violada
     *                 (ex: "Já existe um fundo cadastrado com o ticker HGLG11")
     */
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
