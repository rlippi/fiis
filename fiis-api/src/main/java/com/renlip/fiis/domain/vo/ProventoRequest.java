package com.renlip.fiis.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.renlip.fiis.domain.enumeration.TipoProvento;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

/**
 * VO de entrada para criação/atualização de um Provento.
 *
 * <p>Validação adicional de coerência entre datas (pagamento ≥ referência)
 * é feita no Service, pois envolve comparação entre campos.</p>
 *
 * @param fundoId         ID do fundo pagador
 * @param tipoProvento    tipo (RENDIMENTO ou AMORTIZACAO)
 * @param dataReferencia  data de competência do provento
 * @param dataPagamento   data de pagamento (crédito ao cotista)
 * @param valorPorCota    valor pago por cota (R$)
 * @param quantidadeCotas quantidade de cotas na data de referência
 * @param observacao      observação livre (opcional)
 */
@Schema(description = "Dados para criação/atualização de um Provento")
public record ProventoRequest(

    @NotNull(message = "Fundo é obrigatório")
    @Schema(description = "ID do fundo pagador", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long fundoId,

    @NotNull(message = "Tipo do provento é obrigatório")
    @Schema(description = "Tipo do provento", example = "RENDIMENTO", requiredMode = Schema.RequiredMode.REQUIRED)
    TipoProvento tipoProvento,

    @NotNull(message = "Data de referência é obrigatória")
    @PastOrPresent(message = "Data de referência não pode ser futura")
    @Schema(description = "Data de referência (competência)", example = "2026-03-31", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate dataReferencia,

    @NotNull(message = "Data de pagamento é obrigatória")
    @PastOrPresent(message = "Data de pagamento não pode ser futura")
    @Schema(description = "Data de pagamento", example = "2026-04-15", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate dataPagamento,

    @NotNull(message = "Valor por cota é obrigatório")
    @DecimalMin(value = "0.000001", message = "Valor por cota deve ser maior que zero")
    @Schema(description = "Valor por cota (R$)", example = "1.12", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal valorPorCota,

    @NotNull(message = "Quantidade de cotas é obrigatória")
    @Min(value = 1, message = "Quantidade de cotas deve ser maior ou igual a 1")
    @Schema(description = "Quantidade de cotas na data de referência", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer quantidadeCotas,

    @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
    @Schema(description = "Observação livre", example = "Pago conforme calendário mensal")
    String observacao

) {}
