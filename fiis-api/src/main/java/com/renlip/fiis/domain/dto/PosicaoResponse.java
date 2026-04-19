package com.renlip.fiis.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída representando a <b>posição consolidada</b> em um fundo.
 *
 * <p>É um DTO calculado — não existe tabela de posição. Cada campo é
 * derivado das operações, proventos e cotações do fundo.</p>
 *
 * @param fundo                         fundo resumido (id, ticker, nome)
 * @param quantidadeCotas               cotas atualmente em carteira
 * @param precoMedio                    preço médio por cota (R$)
 * @param custoAtual                    custo atual da posição ({@code qtd × PM})
 * @param totalCompras                  soma bruta investida em compras históricas
 * @param totalVendas                   soma bruta recebida em vendas históricas
 * @param lucroRealizado                lucro/prejuízo realizado nas vendas passadas
 * @param totalProventos                soma dos proventos recebidos (R$)
 * @param yieldSobreCustoPercentual     {@code totalProventos / custoAtual × 100}
 * @param precoAtual                    último preço de mercado (R$)
 * @param dataUltimaCotacao             data da última cotação
 * @param valorAtual                    valor atual da posição ({@code qtd × precoAtual})
 * @param variacaoPercentual            {@code (precoAtual − PM) / PM × 100}
 * @param rentabilidadeTotalPercentual  rentabilidade total considerando valorização + proventos + lucro realizado
 * @param quantidadeOperacoes           total de operações registradas no fundo
 * @param quantidadeProventos           total de proventos recebidos do fundo
 */
@Schema(description = "Posição consolidada em um fundo")
public record PosicaoResponse(

    @Schema(description = "Fundo da posição")
    FundoResumoResponse fundo,

    @Schema(description = "Quantidade de cotas em carteira", example = "12")
    Integer quantidadeCotas,

    @Schema(description = "Preço médio por cota (R$)", example = "151.85")
    BigDecimal precoMedio,

    @Schema(description = "Custo atual da posição (qtd × PM)", example = "1822.20")
    BigDecimal custoAtual,

    @Schema(description = "Total investido em compras históricas (R$)", example = "2278.50")
    BigDecimal totalCompras,

    @Schema(description = "Total recebido em vendas históricas (R$)", example = "479.70")
    BigDecimal totalVendas,

    @Schema(description = "Lucro/prejuízo realizado em vendas (R$)", example = "24.25")
    BigDecimal lucroRealizado,

    @Schema(description = "Total de proventos recebidos (R$)", example = "13.20")
    BigDecimal totalProventos,

    @Schema(description = "Yield sobre custo atual em % (proventos / custoAtual × 100)", example = "0.72")
    BigDecimal yieldSobreCustoPercentual,

    @Schema(description = "Último preço de mercado (R$)", example = "158.50")
    BigDecimal precoAtual,

    @Schema(description = "Data da última cotação", example = "2026-04-17")
    LocalDate dataUltimaCotacao,

    @Schema(description = "Valor atual da posição (qtd × preço atual) em R$", example = "1902.00")
    BigDecimal valorAtual,

    @Schema(description = "Variação % entre preço atual e preço médio", example = "4.34")
    BigDecimal variacaoPercentual,

    @Schema(description = "Rentabilidade total em % (valorização + proventos + lucro realizado)", example = "6.17")
    BigDecimal rentabilidadeTotalPercentual,

    @Schema(description = "Número de operações registradas", example = "3")
    Integer quantidadeOperacoes,

    @Schema(description = "Número de proventos recebidos", example = "1")
    Integer quantidadeProventos

) {}
