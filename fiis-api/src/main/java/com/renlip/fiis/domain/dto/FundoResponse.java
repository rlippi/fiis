package com.renlip.fiis.domain.dto;

import java.time.LocalDateTime;

import com.renlip.fiis.domain.enumeration.Segmento;
import com.renlip.fiis.domain.enumeration.TipoFundo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída representando um Fundo retornado pela API.
 *
 * <p>Inclui todos os campos relevantes para exibição, além das descrições
 * amigáveis dos enums (ex: "Logística" ao invés de apenas "LOGISTICA"),
 * facilitando o trabalho do frontend.</p>
 *
 * @param id                  identificador único
 * @param ticker              código do fundo na B3
 * @param nome                nome oficial
 * @param cnpj                CNPJ (14 dígitos)
 * @param tipo                tipo do fundo (enum)
 * @param tipoDescricao       descrição amigável do tipo
 * @param segmento            segmento (enum)
 * @param segmentoDescricao   descrição amigável do segmento
 * @param ativo               se está ativo na carteira
 * @param dataCriacao         quando foi cadastrado
 * @param dataAtualizacao     última atualização
 */
@Schema(description = "Dados de um Fundo retornado pela API")
public record FundoResponse(

    @Schema(description = "Identificador único", example = "1")
    Long id,

    @Schema(description = "Ticker do fundo na B3", example = "HGLG11")
    String ticker,

    @Schema(description = "Nome oficial do fundo", example = "CSHG Logística FII")
    String nome,

    @Schema(description = "CNPJ do fundo", example = "11728688000147")
    String cnpj,

    @Schema(description = "Tipo do fundo", example = "TIJOLO")
    TipoFundo tipo,

    @Schema(description = "Descrição amigável do tipo", example = "Tijolo")
    String tipoDescricao,

    @Schema(description = "Segmento de atuação", example = "LOGISTICA")
    Segmento segmento,

    @Schema(description = "Descrição amigável do segmento", example = "Logística")
    String segmentoDescricao,

    @Schema(description = "Indica se o fundo está ativo na carteira", example = "true")
    Boolean ativo,

    @Schema(description = "Data de criação do registro")
    LocalDateTime dataCriacao,

    @Schema(description = "Data da última atualização")
    LocalDateTime dataAtualizacao

) {}
