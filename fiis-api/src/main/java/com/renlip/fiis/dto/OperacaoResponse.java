package com.renlip.fiis.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.renlip.fiis.domain.enums.TipoOperacao;
import com.renlip.fiis.domain.model.Operacao;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída representando uma Operação retornada pela API.
 *
 * <p>Inclui o fundo em formato resumido (via {@link FundoResumoResponse}),
 * as descrições amigáveis dos enums e o valor total calculado.</p>
 *
 * @param id              identificador único
 * @param fundo           fundo resumido (id, ticker, nome)
 * @param tipo            tipo (COMPRA/VENDA)
 * @param tipoDescricao   descrição amigável do tipo
 * @param dataOperacao    data da operação
 * @param quantidade      quantidade de cotas
 * @param precoUnitario   preço unitário
 * @param taxas           taxas aplicadas
 * @param valorTotal      valor total calculado (quantidade × preço ± taxas)
 * @param observacao      observação livre
 * @param dataCriacao     data de criação do registro
 * @param dataAtualizacao data da última atualização
 */
@Schema(description = "Operação retornada pela API")
public record OperacaoResponse(

    @Schema(description = "Identificador único", example = "1")
    Long id,

    @Schema(description = "Fundo da operação")
    FundoResumoResponse fundo,

    @Schema(description = "Tipo da operação", example = "COMPRA")
    TipoOperacao tipo,

    @Schema(description = "Descrição amigável do tipo", example = "Compra")
    String tipoDescricao,

    @Schema(description = "Data da operação", example = "2026-04-17")
    LocalDate dataOperacao,

    @Schema(description = "Quantidade de cotas", example = "10")
    Integer quantidade,

    @Schema(description = "Preço unitário por cota (R$)", example = "150.2500")
    BigDecimal precoUnitario,

    @Schema(description = "Taxas aplicadas (R$)", example = "0.50")
    BigDecimal taxas,

    @Schema(description = "Valor total da operação em R$ (qtd × preço ± taxas)", example = "1503.00")
    BigDecimal valorTotal,

    @Schema(description = "Observação", example = "Operação pelo home broker")
    String observacao,

    @Schema(description = "Data de criação do registro")
    LocalDateTime dataCriacao,

    @Schema(description = "Data da última atualização")
    LocalDateTime dataAtualizacao

) {

    /**
     * Converte uma entidade {@link Operacao} em {@link OperacaoResponse}.
     *
     * @param operacao entidade a converter
     * @return DTO populado
     */
    public static OperacaoResponse of(Operacao operacao) {
        return new OperacaoResponse(
            operacao.getId(),
            FundoResumoResponse.of(operacao.getFundo()),
            operacao.getTipo(),
            operacao.getTipo() != null ? operacao.getTipo().getDescricao() : null,
            operacao.getDataOperacao(),
            operacao.getQuantidade(),
            operacao.getPrecoUnitario(),
            operacao.getTaxas(),
            operacao.calcularValorTotal(),
            operacao.getObservacao(),
            operacao.getDataCriacao(),
            operacao.getDataAtualizacao()
        );
    }
}
