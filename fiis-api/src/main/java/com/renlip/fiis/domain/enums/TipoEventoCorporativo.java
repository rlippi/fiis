package com.renlip.fiis.domain.enums;

/**
 * Tipo de evento corporativo que altera a quantidade de cotas de um FII
 * sem ser uma compra ou venda do investidor.
 */
public enum TipoEventoCorporativo {

    /**
     * Bonificação — o cotista recebe cotas adicionais sem custo.
     *
     * <p>Exemplo: bonificação de 10% significa que, para cada 10 cotas,
     * você recebe 1 cota extra. {@code fator = 0.10}.</p>
     *
     * <p><b>Efeito:</b> {@code qty_nova = qty × (1 + fator)}; PM cai proporcionalmente.</p>
     */
    BONIFICACAO("Bonificação"),

    /**
     * Desdobramento (split) — cada cota vira várias.
     *
     * <p>Exemplo: desdobramento 1:10 significa que 1 cota vira 10 cotas.
     * {@code fator = 10}.</p>
     *
     * <p><b>Efeito:</b> {@code qty_nova = qty × fator}; PM é dividido pelo fator.</p>
     */
    DESDOBRAMENTO("Desdobramento"),

    /**
     * Grupamento (reverse split) — várias cotas viram uma.
     *
     * <p>Exemplo: grupamento 10:1 significa que 10 cotas viram 1 cota.
     * {@code fator = 10}.</p>
     *
     * <p><b>Efeito:</b> {@code qty_nova = qty / fator}; PM é multiplicado pelo fator.</p>
     */
    GRUPAMENTO("Grupamento");

    private final String descricao;

    TipoEventoCorporativo(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna a descrição amigável do tipo (para exibição).
     *
     * @return descrição legível (ex: "Bonificação", "Desdobramento")
     */
    public String getDescricao() {
        return descricao;
    }
}
