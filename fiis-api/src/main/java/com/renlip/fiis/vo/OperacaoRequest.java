package com.renlip.fiis.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.renlip.fiis.domain.enums.TipoOperacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

/**
 * VO de entrada para criação/atualização de uma Operação.
 *
 * <p>Recebe apenas o ID do fundo (não o objeto completo), mantendo o
 * contrato da API simples. O Service se encarrega de carregar a entidade
 * {@link com.renlip.fiis.domain.model.Fundo} pelo ID.</p>
 *
 * @param fundoId       ID do fundo envolvido
 * @param tipo          tipo da operação (COMPRA/VENDA)
 * @param dataOperacao  data em que a operação foi realizada (não futura)
 * @param quantidade    quantidade de cotas (≥ 1)
 * @param precoUnitario preço por cota (> 0)
 * @param taxas         total de taxas (≥ 0, opcional — padrão 0)
 * @param observacao    observação livre (opcional, máx 255)
 */
@Schema(description = "Dados para criação/atualização de uma Operação")
public record OperacaoRequest(

    @NotNull(message = "Fundo é obrigatório")
    @Schema(description = "ID do fundo envolvido", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long fundoId,

    @NotNull(message = "Tipo é obrigatório")
    @Schema(description = "Tipo da operação", example = "COMPRA", requiredMode = Schema.RequiredMode.REQUIRED)
    TipoOperacao tipo,

    @NotNull(message = "Data da operação é obrigatória")
    @PastOrPresent(message = "Data da operação não pode ser futura")
    @Schema(description = "Data da operação", example = "2026-04-17", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate dataOperacao,

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior ou igual a 1")
    @Schema(description = "Quantidade de cotas", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer quantidade,

    @NotNull(message = "Preço unitário é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço unitário deve ser maior que zero")
    @Schema(description = "Preço unitário por cota (R$)", example = "150.25", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal precoUnitario,

    @DecimalMin(value = "0.00", inclusive = true, message = "Taxas não podem ser negativas")
    @Schema(description = "Taxas (corretagem + emolumentos) em R$", example = "0.50")
    BigDecimal taxas,

    @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
    @Schema(description = "Observação livre", example = "Operação pelo home broker")
    String observacao

) {}
