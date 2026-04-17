package com.renlip.fiis.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.renlip.fiis.domain.dto.OperacaoResponse;
import com.renlip.fiis.exception.ErroResponse;
import com.renlip.fiis.service.OperacaoService;
import com.renlip.fiis.domain.vo.OperacaoRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller REST para operações de compra e venda de cotas de FIIs.
 */
@RestController
@RequestMapping("/api/operacoes")
@RequiredArgsConstructor
@Tag(name = "Operações", description = "Operações de compra e venda de cotas de FIIs")
public class OperacaoController {

    private final OperacaoService operacaoService;

    /**
     * Lista operações. Se {@code fundoId} for informado, filtra pelo fundo.
     *
     * @param fundoId opcional — ID do fundo para filtrar
     * @return lista de operações
     */
    @GetMapping
    @Operation(
        summary = "Lista operações",
        description = "Retorna todas as operações cadastradas. Use fundoId para filtrar por fundo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Fundo informado não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<List<OperacaoResponse>> listar(
            @Parameter(description = "Filtrar por ID do fundo (opcional)")
            @RequestParam(name = "fundoId", required = false) Long fundoId) {

        List<OperacaoResponse> operacoes = fundoId != null
            ? operacaoService.listarPorFundo(fundoId)
            : operacaoService.listarTodas();

        return ResponseEntity.ok(operacoes);
    }

    /**
     * Busca uma operação pelo ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Busca operação por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação encontrada"),
        @ApiResponse(responseCode = "404", description = "Operação não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<OperacaoResponse> buscarPorId(
            @Parameter(description = "ID da operação", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(operacaoService.buscarPorId(id));
    }

    /**
     * Cria uma nova operação.
     */
    @PostMapping
    @Operation(summary = "Cria uma nova operação")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Operação criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "404", description = "Fundo não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "409", description = "Venda excede a posição em carteira",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<OperacaoResponse> criar(@Valid @RequestBody OperacaoRequest request) {
        OperacaoResponse criada = operacaoService.criar(request);

        URI location = UriComponentsBuilder
            .fromPath("/api/operacoes/{id}")
            .buildAndExpand(criada.id())
            .toUri();

        return ResponseEntity.created(location).body(criada);
    }

    /**
     * Atualiza uma operação existente.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma operação existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "404", description = "Operação ou fundo não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "409", description = "Edição resultaria em posição negativa",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<OperacaoResponse> atualizar(
            @Parameter(description = "ID da operação", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody OperacaoRequest request) {
        return ResponseEntity.ok(operacaoService.atualizar(id, request));
    }

    /**
     * Remove uma operação (hard delete).
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Remove uma operação",
        description = "Remove permanentemente a operação do banco."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Operação removida com sucesso"),
        @ApiResponse(responseCode = "404", description = "Operação não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da operação", example = "1")
            @PathVariable Long id) {
        operacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
