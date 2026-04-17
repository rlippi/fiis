package com.renlip.fiis.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.renlip.fiis.domain.enumeration.TipoEventoCorporativo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída representando um evento corporativo retornado pela API.
 *
 * @param id              identificador único
 * @param fundo           fundo resumido (id, ticker, nome)
 * @param tipo            tipo do evento
 * @param tipoDescricao   descrição amigável do tipo
 * @param data            data do evento
 * @param fator           fator do evento
 * @param descricao       descrição
 * @param dataCriacao     data de criação do registro
 * @param dataAtualizacao data da última atualização
 */
@Schema(description = "Evento corporativo retornado pela API")
public record EventoCorporativoResponse(

    @Schema(description = "Identificador único", example = "1")
    Long id,

    @Schema(description = "Fundo do evento")
    FundoResumoResponse fundo,

    @Schema(description = "Tipo do evento", example = "DESDOBRAMENTO")
    TipoEventoCorporativo tipo,

    @Schema(description = "Descrição amigável do tipo", example = "Desdobramento")
    String tipoDescricao,

    @Schema(description = "Data do evento", example = "2026-04-10")
    LocalDate data,

    @Schema(description = "Fator do evento", example = "10")
    BigDecimal fator,

    @Schema(description = "Descrição livre", example = "Desdobramento 1:10 aprovado em AGE")
    String descricao,

    @Schema(description = "Data de criação do registro")
    LocalDateTime dataCriacao,

    @Schema(description = "Data da última atualização")
    LocalDateTime dataAtualizacao

) {}
