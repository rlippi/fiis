package com.renlip.fiis.domain.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída representando a renda passiva agregada de um mês
 * (com base na data de pagamento dos proventos).
 *
 * @param ano              ano de referência
 * @param mes              mês de referência (1-12)
 * @param nomeMes          nome do mês em português (ex: "Abril")
 * @param totalRecebido    soma dos proventos recebidos no mês (R$)
 * @param quantidadeProventos quantos proventos foram pagos no mês
 */
@Schema(description = "Renda passiva consolidada de um mês")
public record RendaMensalResponse(

    @Schema(description = "Ano de referência", example = "2026")
    Integer ano,

    @Schema(description = "Mês de referência (1-12)", example = "4")
    Integer mes,

    @Schema(description = "Nome do mês em português", example = "Abril")
    String nomeMes,

    @Schema(description = "Total recebido no mês (R$)", example = "23.20")
    BigDecimal totalRecebido,

    @Schema(description = "Quantidade de proventos pagos no mês", example = "2")
    Integer quantidadeProventos

) {}
