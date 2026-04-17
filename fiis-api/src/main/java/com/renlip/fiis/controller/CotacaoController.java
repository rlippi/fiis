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

import com.renlip.fiis.dto.CotacaoResponse;
import com.renlip.fiis.exception.ErroResponse;
import com.renlip.fiis.service.CotacaoService;
import com.renlip.fiis.vo.CotacaoRequest;

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
 * Controller REST para cotações históricas de FIIs.
 */
@RestController
@RequestMapping("/api/cotacoes")
@RequiredArgsConstructor
@Tag(name = "Cotações", description = "Histórico de cotações (preços de mercado) dos FIIs")
public class CotacaoController {

    private final CotacaoService cotacaoService;

    /**
     * Lista cotações. Se {@code fundoId} for informado, filtra pelo fundo.
     *
     * @param fundoId opcional — ID do fundo
     * @return lista de cotações
     */
    @GetMapping
    @Operation(
        summary = "Lista cotações",
        description = "Retorna todas as cotações cadastradas. Use fundoId para filtrar por fundo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Fundo informado não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<List<CotacaoResponse>> listar(
            @Parameter(description = "Filtrar por ID do fundo")
            @RequestParam(required = false) Long fundoId) {

        List<CotacaoResponse> cotacoes = fundoId != null
            ? cotacaoService.listarPorFundo(fundoId)
            : cotacaoService.listarTodas();

        return ResponseEntity.ok(cotacoes);
    }

    /**
     * Busca uma cotação pelo ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Busca cotação por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cotação encontrada"),
        @ApiResponse(responseCode = "404", description = "Cotação não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<CotacaoResponse> buscarPorId(
            @Parameter(description = "ID da cotação", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(cotacaoService.buscarPorId(id));
    }

    /**
     * Retorna a cotação mais recente de um fundo.
     *
     * @param fundoId ID do fundo
     * @return última cotação
     */
    @GetMapping("/ultima/{fundoId}")
    @Operation(
        summary = "Última cotação de um fundo",
        description = "Retorna a cotação com a data mais recente para o fundo informado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cotação encontrada"),
        @ApiResponse(responseCode = "404", description = "Fundo não encontrado ou sem cotações",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<CotacaoResponse> ultimaCotacao(
            @Parameter(description = "ID do fundo", example = "1")
            @PathVariable Long fundoId) {
        return cotacaoService.buscarUltimaCotacao(fundoId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cria uma nova cotação.
     */
    @PostMapping
    @Operation(summary = "Cria uma nova cotação")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cotação criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "404", description = "Fundo não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "409", description = "Cotação duplicada ou mínimo > máximo",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<CotacaoResponse> criar(@Valid @RequestBody CotacaoRequest request) {
        CotacaoResponse criada = cotacaoService.criar(request);

        URI location = UriComponentsBuilder
            .fromPath("/api/cotacoes/{id}")
            .buildAndExpand(criada.id())
            .toUri();

        return ResponseEntity.created(location).body(criada);
    }

    /**
     * Atualiza uma cotação existente.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma cotação existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cotação atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cotação ou fundo não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "409", description = "Cotação duplicada ou mínimo > máximo",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<CotacaoResponse> atualizar(
            @Parameter(description = "ID da cotação", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CotacaoRequest request) {
        return ResponseEntity.ok(cotacaoService.atualizar(id, request));
    }

    /**
     * Remove uma cotação (hard delete).
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma cotação")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cotação removida com sucesso"),
        @ApiResponse(responseCode = "404", description = "Cotação não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da cotação", example = "1")
            @PathVariable Long id) {
        cotacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
