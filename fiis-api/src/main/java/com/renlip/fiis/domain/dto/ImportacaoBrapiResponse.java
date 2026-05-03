package com.renlip.fiis.domain.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta da operação de importação em lote de cotações via BRAPI.
 *
 * <p>Indica quantos fundos foram processados, quantos tiveram cotação nova
 * criada, quantos tiveram a cotação do dia atualizada (upsert) e a lista de
 * tickers enviados à BRAPI que não vieram no resultado (fundos inexistentes
 * no mercado ou rate limit parcial).</p>
 */
@Schema(description = "Resumo da importação de cotações via BRAPI")
public record ImportacaoBrapiResponse(

    @Schema(description = "Total de fundos ativos processados", example = "4")
    int totalFundos,

    @Schema(description = "Cotações criadas no dia atual", example = "3")
    int criados,

    @Schema(description = "Cotações do dia atual que foram atualizadas (upsert)", example = "1")
    int atualizados,

    @Schema(description = "Tickers que a BRAPI não retornou", example = "[\"VISC11\"]")
    List<String> naoEncontradosBrapi
) {
}
