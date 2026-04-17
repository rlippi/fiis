package com.renlip.fiis.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.renlip.fiis.dto.PosicaoResponse;
import com.renlip.fiis.exception.ErroResponse;
import com.renlip.fiis.service.PosicaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controller REST para consulta de posição consolidada por fundo.
 *
 * <p>Expõe apenas operações de leitura. A posição é calculada sob demanda
 * a partir das operações e proventos registrados.</p>
 */
@RestController
@RequestMapping("/api/posicoes")
@RequiredArgsConstructor
@Tag(name = "Posições", description = "Posição consolidada em cada fundo (qtd, PM, proventos, yield)")
public class PosicaoController {

    private final PosicaoService posicaoService;

    /**
     * Retorna a posição consolidada de todos os fundos ativos.
     *
     * @return lista de posições (uma por fundo ativo)
     */
    @GetMapping
    @Operation(
        summary = "Lista posições de todos os fundos ativos",
        description = "Calcula, para cada fundo ativo na carteira, quantidade, PM, custo, lucro realizado e proventos."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Posições calculadas com sucesso")
    })
    public ResponseEntity<List<PosicaoResponse>> listar() {
        return ResponseEntity.ok(posicaoService.calcularPosicaoDeTodos());
    }

    /**
     * Retorna a posição consolidada de um fundo específico.
     *
     * @param fundoId ID do fundo
     * @return posição consolidada
     */
    @GetMapping("/{fundoId}")
    @Operation(summary = "Posição consolidada de um fundo específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Posição calculada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Fundo não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<PosicaoResponse> buscarPorFundo(
            @Parameter(description = "ID do fundo", example = "1")
            @PathVariable Long fundoId) {
        return ResponseEntity.ok(posicaoService.calcularPosicaoDoFundo(fundoId));
    }
}
