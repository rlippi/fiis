package com.renlip.fiis.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.renlip.fiis.domain.enums.TipoEventoCorporativo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

/**
 * VO de entrada para criação/atualização de um Evento Corporativo.
 *
 * @param fundoId   ID do fundo do evento
 * @param tipo      tipo do evento (BONIFICACAO, DESDOBRAMENTO, GRUPAMENTO)
 * @param data      data do evento (não futura)
 * @param fator     fator do evento (significado depende do tipo — ver {@link TipoEventoCorporativo})
 * @param descricao descrição livre (opcional)
 */
@Schema(description = "Dados para criação/atualização de um Evento Corporativo")
public record EventoCorporativoRequest(

    @NotNull(message = "Fundo é obrigatório")
    @Schema(description = "ID do fundo", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long fundoId,

    @NotNull(message = "Tipo do evento é obrigatório")
    @Schema(description = "Tipo do evento", example = "DESDOBRAMENTO", requiredMode = Schema.RequiredMode.REQUIRED)
    TipoEventoCorporativo tipo,

    @NotNull(message = "Data é obrigatória")
    @PastOrPresent(message = "Data do evento não pode ser futura")
    @Schema(description = "Data do evento (data ex)", example = "2026-04-10", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate data,

    @NotNull(message = "Fator é obrigatório")
    @DecimalMin(value = "0.000001", message = "Fator deve ser maior que zero")
    @Schema(
        description = "Fator do evento: BONIFICACAO=proporção (0.10 = +10%), DESDOBRAMENTO=multiplicador (10 = 1:10), GRUPAMENTO=divisor (10 = 10:1)",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    BigDecimal fator,

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    @Schema(description = "Descrição livre", example = "Desdobramento 1:10 aprovado em AGE")
    String descricao

) {}
