package com.renlip.fiis.domain.dto;

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

) {}
