package com.renlip.fiis.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída representando a renda passiva consolidada de um fundo
 * (soma de todos os proventos recebidos deste fundo).
 *
 * @param fundo               fundo resumido (id, ticker, nome)
 * @param totalRecebido       total recebido em proventos (R$)
 * @param quantidadeProventos quantidade de proventos pagos pelo fundo
 */
@Schema(description = "Renda passiva consolidada por fundo")
public record RendaPorFundoResponse(

    @Schema(description = "Fundo pagador")
    FundoResumoResponse fundo,

    @Schema(description = "Total recebido em proventos (R$)", example = "13.20")
    BigDecimal totalRecebido,

    @Schema(description = "Quantidade de proventos pagos", example = "1")
    Integer quantidadeProventos

) {}
