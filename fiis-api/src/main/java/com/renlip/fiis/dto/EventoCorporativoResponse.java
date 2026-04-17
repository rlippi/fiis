package com.renlip.fiis.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.renlip.fiis.domain.enums.TipoEventoCorporativo;
import com.renlip.fiis.domain.model.EventoCorporativo;

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

) {

    /**
     * Converte uma entidade {@link EventoCorporativo} em {@link EventoCorporativoResponse}.
     *
     * @param evento entidade a converter
     * @return DTO populado
     */
    public static EventoCorporativoResponse of(EventoCorporativo evento) {
        return new EventoCorporativoResponse(
            evento.getId(),
            FundoResumoResponse.of(evento.getFundo()),
            evento.getTipo(),
            evento.getTipo() != null ? evento.getTipo().getDescricao() : null,
            evento.getData(),
            evento.getFator(),
            evento.getDescricao(),
            evento.getDataCriacao(),
            evento.getDataAtualizacao()
        );
    }
}
