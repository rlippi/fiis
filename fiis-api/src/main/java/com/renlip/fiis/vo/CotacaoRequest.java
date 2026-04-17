package com.renlip.fiis.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

/**
 * VO de entrada para criação/atualização de uma Cotação.
 *
 * <p>Validação de coerência entre {@code precoMinimo} e {@code precoMaximo}
 * é feita no Service (mínimo deve ser ≤ máximo).</p>
 *
 * @param fundoId         ID do fundo
 * @param data            data da cotação (não futura)
 * @param precoFechamento preço de fechamento (obrigatório, > 0)
 * @param precoAbertura   preço de abertura (opcional, > 0)
 * @param precoMinimo     preço mínimo do pregão (opcional, > 0)
 * @param precoMaximo     preço máximo do pregão (opcional, > 0)
 * @param volume          volume financeiro negociado (opcional, ≥ 0)
 */
@Schema(description = "Dados para criação/atualização de uma Cotação")
public record CotacaoRequest(

    @NotNull(message = "Fundo é obrigatório")
    @Schema(description = "ID do fundo", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long fundoId,

    @NotNull(message = "Data é obrigatória")
    @PastOrPresent(message = "Data da cotação não pode ser futura")
    @Schema(description = "Data do pregão", example = "2026-04-17", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate data,

    @NotNull(message = "Preço de fechamento é obrigatório")
    @DecimalMin(value = "0.0001", message = "Preço de fechamento deve ser maior que zero")
    @Schema(description = "Preço de fechamento (R$)", example = "160.25", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal precoFechamento,

    @DecimalMin(value = "0.0001", message = "Preço de abertura deve ser maior que zero")
    @Schema(description = "Preço de abertura (R$)", example = "158.90")
    BigDecimal precoAbertura,

    @DecimalMin(value = "0.0001", message = "Preço mínimo deve ser maior que zero")
    @Schema(description = "Menor preço do pregão (R$)", example = "158.00")
    BigDecimal precoMinimo,

    @DecimalMin(value = "0.0001", message = "Preço máximo deve ser maior que zero")
    @Schema(description = "Maior preço do pregão (R$)", example = "161.50")
    BigDecimal precoMaximo,

    @DecimalMin(value = "0.00", inclusive = true, message = "Volume não pode ser negativo")
    @Schema(description = "Volume financeiro (R$)", example = "1250000.00")
    BigDecimal volume

) {}
