package com.renlip.fiis.domain.enums;

/**
 * Tipo de uma operação realizada com cotas de um FII.
 */
public enum TipoOperacao {

    /** Compra de cotas — aumenta a posição na carteira. */
    COMPRA("Compra"),

    /** Venda de cotas — diminui a posição na carteira. */
    VENDA("Venda");

    private final String descricao;

    TipoOperacao(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna a descrição amigável do tipo (para exibição).
     *
     * @return descrição legível (ex: "Compra", "Venda")
     */
    public String getDescricao() {
        return descricao;
    }
}
