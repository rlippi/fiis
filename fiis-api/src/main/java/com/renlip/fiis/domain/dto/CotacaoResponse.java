package com.renlip.fiis.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída representando uma cotação retornada pela API.
 *
 * @param id              identificador único
 * @param fundo           fundo resumido (id, ticker, nome)
 * @param data            data do pregão
 * @param precoFechamento preço de fechamento (R$)
 * @param precoAbertura   preço de abertura (R$)
 * @param precoMinimo     menor preço do pregão (R$)
 * @param precoMaximo     maior preço do pregão (R$)
 * @param volume          volume financeiro (R$)
 * @param dataCriacao     data de criação do registro
 * @param dataAtualizacao data da última atualização
 */
@Schema(description = "Cotação retornada pela API")
public record CotacaoResponse(

    @Schema(description = "Identificador único", example = "1")
    Long id,

    @Schema(description = "Fundo da cotação")
    FundoResumoResponse fundo,

    @Schema(description = "Data do pregão", example = "2026-04-17")
    LocalDate data,

    @Schema(description = "Preço de fechamento (R$)", example = "160.2500")
    BigDecimal precoFechamento,

    @Schema(description = "Preço de abertura (R$)", example = "158.9000")
    BigDecimal precoAbertura,

    @Schema(description = "Menor preço do pregão (R$)", example = "158.0000")
    BigDecimal precoMinimo,

    @Schema(description = "Maior preço do pregão (R$)", example = "161.5000")
    BigDecimal precoMaximo,

    @Schema(description = "Volume financeiro (R$)", example = "1250000.00")
    BigDecimal volume,

    @Schema(description = "Data de criação do registro")
    LocalDateTime dataCriacao,

    @Schema(description = "Data da última atualização")
    LocalDateTime dataAtualizacao

) {}
