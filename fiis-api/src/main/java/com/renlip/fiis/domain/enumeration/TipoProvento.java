package com.renlip.fiis.domain.enumeration;

/**
 * Tipo de provento pago por um FII ao cotista.
 */
public enum TipoProvento {

    /**
     * Rendimento distribuído ao cotista.
     *
     * <p>É a principal fonte de renda passiva dos FIIs. Para pessoas físicas
     * que atendem aos requisitos legais, este valor é <b>isento de Imposto
     * de Renda</b>.</p>
     */
    RENDIMENTO("Rendimento"),

    /**
     * Amortização (devolução de capital) ao cotista.
     *
     * <p>Reduz o custo médio da posição e não é tributado como rendimento.
     * Comum em fundos que vendem ativos e distribuem o ganho de capital.</p>
     */
    AMORTIZACAO("Amortização");

    private final String descricao;

    TipoProvento(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna a descrição amigável do tipo (para exibição).
     *
     * @return descrição legível (ex: "Rendimento", "Amortização")
     */
    public String getDescricao() {
        return descricao;
    }
}
