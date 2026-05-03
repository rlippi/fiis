package com.renlip.fiis.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resumo consolidado da execução do job de atualização de cotações.
 *
 * <p>Diferente de {@link ImportacaoBrapiResponse} (que descreve 1 carteira),
 * este DTO agrega os totais do job que itera sobre todos os usuários ativos.</p>
 */
@Schema(description = "Resumo consolidado da execução de um job em lote")
public record ResumoJobResponse(

    @Schema(description = "Total de usuários ativos processados", example = "3")
    int usuariosProcessados,

    @Schema(description = "Usuários cujas cotações foram atualizadas com sucesso", example = "2")
    int comSucesso,

    @Schema(description = "Usuários em que a atualização falhou (ver logs para detalhes)", example = "1")
    int comFalha,

    @Schema(description = "Total de cotações criadas somadas entre todos os usuários", example = "6")
    int cotacoesCriadas,

    @Schema(description = "Total de cotações atualizadas (upsert) somadas entre todos os usuários", example = "2")
    int cotacoesAtualizadas
) {
}
