package com.renlip.fiis.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída com o resumo geral da carteira (totais consolidados).
 *
 * <p>Serve de ponto de partida do dashboard — mostra a "foto" da carteira
 * numa única chamada.</p>
 *
 * @param quantidadeFundosAtivos    quantidade de fundos ativos
 * @param quantidadeFundosComPosicao fundos que ainda têm cotas em carteira
 * @param custoTotalCarteira        soma do custo atual de todas as posições (R$)
 * @param totalInvestidoHistorico   soma de todas as compras já realizadas (R$)
 * @param totalVendasHistorico      soma de todas as vendas já realizadas (R$)
 * @param lucroRealizadoTotal       lucro/prejuízo total realizado em vendas (R$)
 * @param totalProventosRecebidos   soma total de proventos recebidos (R$)
 * @param yieldCarteiraPercentual   yield sobre o custo atual da carteira (%)
 * @param mediaProventosMensal      média mensal de proventos recebidos (R$)
 * @param mesesComProventos         número de meses em que houve recebimento
 */
@Schema(description = "Resumo geral da carteira")
public record ResumoCarteiraResponse(

    @Schema(description = "Quantidade de fundos ativos", example = "2")
    Integer quantidadeFundosAtivos,

    @Schema(description = "Fundos com posição em carteira (qtd > 0)", example = "1")
    Integer quantidadeFundosComPosicao,

    @Schema(description = "Custo atual total da carteira (R$)", example = "1822.80")
    BigDecimal custoTotalCarteira,

    @Schema(description = "Total investido em compras (histórico)", example = "2278.50")
    BigDecimal totalInvestidoHistorico,

    @Schema(description = "Total recebido em vendas (histórico)", example = "479.70")
    BigDecimal totalVendasHistorico,

    @Schema(description = "Lucro/prejuízo realizado em vendas (R$)", example = "24.00")
    BigDecimal lucroRealizadoTotal,

    @Schema(description = "Total de proventos recebidos (R$)", example = "23.20")
    BigDecimal totalProventosRecebidos,

    @Schema(description = "Yield sobre o custo atual (%)", example = "1.27")
    BigDecimal yieldCarteiraPercentual,

    @Schema(description = "Média mensal de proventos (R$)", example = "23.20")
    BigDecimal mediaProventosMensal,

    @Schema(description = "Quantidade de meses com recebimento", example = "1")
    Integer mesesComProventos

) {}
