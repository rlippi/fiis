package com.renlip.fiis.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.renlip.fiis.domain.enumeration.TipoProvento;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída representando um Provento retornado pela API.
 *
 * @param id                     identificador único
 * @param fundo                  fundo resumido (id, ticker, nome)
 * @param tipoProvento           tipo (RENDIMENTO/AMORTIZACAO)
 * @param tipoProventoDescricao  descrição amigável
 * @param dataReferencia         competência do provento
 * @param dataPagamento          data em que foi pago
 * @param valorPorCota           valor pago por cota
 * @param quantidadeCotas        quantidade de cotas na referência
 * @param valorTotal             total recebido (calculado)
 * @param observacao             observação livre
 * @param dataCriacao            data de criação do registro
 * @param dataAtualizacao        data da última atualização
 */
@Schema(description = "Provento retornado pela API")
public record ProventoResponse(

    @Schema(description = "Identificador único", example = "1")
    Long id,

    @Schema(description = "Fundo pagador")
    FundoResumoResponse fundo,

    @Schema(description = "Tipo do provento", example = "RENDIMENTO")
    TipoProvento tipoProvento,

    @Schema(description = "Descrição amigável do tipo", example = "Rendimento")
    String tipoProventoDescricao,

    @Schema(description = "Data de referência (competência)", example = "2026-03-31")
    LocalDate dataReferencia,

    @Schema(description = "Data de pagamento", example = "2026-04-15")
    LocalDate dataPagamento,

    @Schema(description = "Valor pago por cota (R$)", example = "1.120000")
    BigDecimal valorPorCota,

    @Schema(description = "Quantidade de cotas na data de referência", example = "12")
    Integer quantidadeCotas,

    @Schema(description = "Valor total recebido (R$)", example = "13.44")
    BigDecimal valorTotal,

    @Schema(description = "Observação", example = "Pago conforme calendário mensal")
    String observacao,

    @Schema(description = "Data de criação do registro")
    LocalDateTime dataCriacao,

    @Schema(description = "Data da última atualização")
    LocalDateTime dataAtualizacao

) {}
