package com.renlip.fiis.domain.enumeration;

/**
 * Classificação primária de um Fundo de Investimento Imobiliário (FII)
 * quanto à natureza dos seus investimentos.
 *
 * <p>Esta é a classificação mais macro de um FII, usada por investidores
 * para entender o perfil de risco e retorno do fundo.</p>
 */
public enum TipoFundo {

    /**
     * Fundos que investem em imóveis físicos (galpões, shoppings, lajes,
     * hospitais, etc.) e geram renda através do aluguel desses imóveis.
     */
    TIJOLO("Tijolo"),

    /**
     * Fundos que investem em títulos de dívida imobiliária, como
     * Certificados de Recebíveis Imobiliários (CRI), Letras de Crédito
     * Imobiliário (LCI) e Letras Hipotecárias (LH).
     */
    PAPEL("Papel"),

    /**
     * Fundos que misturam investimentos em imóveis físicos e em títulos
     * de dívida imobiliária na mesma carteira.
     */
    HIBRIDO("Híbrido"),

    /**
     * Fundos que investem em cotas de outros FIIs, conhecidos como FoFs
     * (Fund of Funds). Oferecem diversificação automática.
     */
    FUNDO_DE_FUNDOS("Fundo de Fundos");

    private final String descricao;

    TipoFundo(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna a descrição amigável do tipo (para exibição).
     *
     * @return descrição legível (ex: "Tijolo", "Fundo de Fundos")
     */
    public String getDescricao() {
        return descricao;
    }
}
