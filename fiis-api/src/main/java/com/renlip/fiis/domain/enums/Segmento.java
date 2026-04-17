package com.renlip.fiis.domain.enums;

/**
 * Segmento de atuação de um Fundo de Investimento Imobiliário (FII).
 *
 * <p>Representa a área específica onde o fundo investe (ex: logística,
 * shoppings, lajes corporativas). É uma classificação mais granular
 * que o {@link TipoFundo}.</p>
 */
public enum Segmento {

    /** Galpões logísticos, centros de distribuição e condomínios industriais. */
    LOGISTICA("Logística"),

    /** Shopping centers e centros comerciais. */
    SHOPPING("Shopping"),

    /** Prédios comerciais, escritórios e salas comerciais. */
    LAJES_CORPORATIVAS("Lajes Corporativas"),

    /** Imóveis residenciais (apartamentos, casas). */
    RESIDENCIAL("Residencial"),

    /** Hospitais, clínicas e unidades de saúde. */
    HOSPITALAR("Hospitalar"),

    /** Faculdades, escolas e unidades educacionais. */
    EDUCACIONAL("Educacional"),

    /** Imóveis alugados para agências bancárias. */
    AGENCIAS_BANCARIAS("Agências Bancárias"),

    /** Hotéis, flats e resorts. */
    HOTEIS("Hotéis"),

    /** Títulos de recebíveis imobiliários (CRI, LCI, LH) - usado em FIIs de Papel. */
    RECEBIVEIS("Recebíveis"),

    /** Fundos que investem em múltiplos segmentos simultaneamente. */
    MULTISEGMENTO("Multissegmento"),

    /** Fundos que investem em cotas de outros FIIs. */
    FUNDO_DE_FUNDOS("Fundo de Fundos");

    private final String descricao;

    Segmento(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna a descrição amigável do segmento (para exibição).
     *
     * @return descrição legível (ex: "Logística", "Lajes Corporativas")
     */
    public String getDescricao() {
        return descricao;
    }
}
