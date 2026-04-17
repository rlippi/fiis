package com.renlip.fiis.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída representando a alocação da carteira em uma categoria
 * (tipo ou segmento).
 *
 * <p>Usado para alimentar gráficos de distribuição no dashboard.</p>
 *
 * @param categoriaCodigo    código da categoria (ex: "LOGISTICA", "TIJOLO")
 * @param categoriaDescricao descrição amigável (ex: "Logística", "Tijolo")
 * @param custoAtual         soma do custo atual dos fundos na categoria (R$)
 * @param percentual         participação da categoria no total da carteira (%)
 * @param quantidadeFundos   quantos fundos estão nessa categoria
 */
@Schema(description = "Alocação da carteira em uma categoria")
public record AlocacaoResponse(

    @Schema(description = "Código da categoria (enum)", example = "LOGISTICA")
    String categoriaCodigo,

    @Schema(description = "Descrição amigável da categoria", example = "Logística")
    String categoriaDescricao,

    @Schema(description = "Custo atual dos fundos na categoria (R$)", example = "1822.80")
    BigDecimal custoAtual,

    @Schema(description = "Percentual da categoria no total (%)", example = "100.00")
    BigDecimal percentual,

    @Schema(description = "Quantidade de fundos na categoria", example = "1")
    Integer quantidadeFundos

) {}
