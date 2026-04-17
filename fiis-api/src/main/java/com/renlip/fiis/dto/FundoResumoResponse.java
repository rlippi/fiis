package com.renlip.fiis.dto;

import com.renlip.fiis.domain.model.Fundo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO resumido de um Fundo, usado para ser embutido em outros DTOs
 * (operação, provento, posição, etc.).
 *
 * <p>Contém apenas informações de identificação mínimas, evitando
 * trafegar dados pesados e desnecessários.</p>
 *
 * @param id     identificador do fundo
 * @param ticker código na B3
 * @param nome   nome oficial
 */
@Schema(description = "Resumo de um Fundo (id, ticker e nome)")
public record FundoResumoResponse(

    @Schema(description = "Identificador do fundo", example = "1")
    Long id,

    @Schema(description = "Ticker do fundo na B3", example = "HGLG11")
    String ticker,

    @Schema(description = "Nome do fundo", example = "CSHG Logística FII")
    String nome

) {

    /**
     * Factory method para converter uma entidade {@link Fundo} em resumo.
     *
     * @param fundo entidade a converter
     * @return DTO resumido ou {@code null} se o parâmetro for nulo
     */
    public static FundoResumoResponse of(Fundo fundo) {
        if (fundo == null) {
            return null;
        }
        return new FundoResumoResponse(fundo.getId(), fundo.getTicker(), fundo.getNome());
    }
}
