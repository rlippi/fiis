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

import com.renlip.fiis.dto.FundoResponse;
import com.renlip.fiis.exception.ErroResponse;
import com.renlip.fiis.service.FundoService;
import com.renlip.fiis.vo.FundoRequest;

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
 * Controller REST de Fundos de Investimento Imobiliário (FIIs).
 *
 * <p>Expõe os endpoints de gerenciamento dos fundos cadastrados na carteira,
 * incluindo listagem, busca, criação, atualização e desativação.</p>
 *
 * <p>Todas as operações retornam {@link FundoResponse} como saída e recebem
 * {@link FundoRequest} como entrada (quando aplicável).</p>
 */
@RestController
@RequestMapping("/api/fundos")
@RequiredArgsConstructor
@Tag(name = "Fundos", description = "Operações de CRUD de Fundos de Investimento Imobiliário")
public class FundoController {

    private final FundoService fundoService;

    /**
     * Lista todos os fundos. Filtro opcional por status ativo.
     *
     * @param apenasAtivos se {@code true}, retorna apenas os fundos ativos
     * @return lista de fundos
     */
    @GetMapping
    @Operation(
        summary = "Lista fundos",
        description = "Retorna todos os fundos cadastrados. Use apenasAtivos=true para filtrar só os ativos."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<FundoResponse>> listar(
            @Parameter(description = "Se true, retorna apenas fundos ativos")
            @RequestParam(name = "apenasAtivos", defaultValue = "false") boolean apenasAtivos) {

        List<FundoResponse> fundos = apenasAtivos
            ? fundoService.listarAtivos()
            : fundoService.listarTodos();

        return ResponseEntity.ok(fundos);
    }

    /**
     * Busca um fundo pelo seu ID.
     *
     * @param id identificador do fundo
     * @return dados do fundo encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Busca fundo por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fundo encontrado"),
        @ApiResponse(responseCode = "404", description = "Fundo não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<FundoResponse> buscarPorId(
            @Parameter(description = "ID do fundo", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(fundoService.buscarPorId(id));
    }

    /**
     * Cria um novo fundo.
     *
     * <p>Retorna HTTP 201 (Created) com o fundo criado no body e o header
     * {@code Location} apontando para o recurso recém-criado.</p>
     *
     * @param request dados do novo fundo
     * @return fundo criado
     */
    @PostMapping
    @Operation(summary = "Cria um novo fundo")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Fundo criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "409", description = "Ticker já cadastrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<FundoResponse> criar(@Valid @RequestBody FundoRequest request) {
        FundoResponse criado = fundoService.criar(request);

        URI location = UriComponentsBuilder
            .fromPath("/api/fundos/{id}")
            .buildAndExpand(criado.id())
            .toUri();

        return ResponseEntity.created(location).body(criado);
    }

    /**
     * Atualiza um fundo existente.
     *
     * @param id      identificador do fundo
     * @param request novos dados
     * @return fundo atualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um fundo existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fundo atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "404", description = "Fundo não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "409", description = "Ticker já cadastrado por outro fundo",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<FundoResponse> atualizar(
            @Parameter(description = "ID do fundo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody FundoRequest request) {
        return ResponseEntity.ok(fundoService.atualizar(id, request));
    }

    /**
     * Desativa um fundo (soft delete).
     *
     * <p>O registro é mantido no banco, apenas marcado como inativo.
     * Para reativar, use o endpoint de atualização informando {@code ativo = true}.</p>
     *
     * @param id identificador do fundo
     * @return HTTP 204 (No Content)
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Desativa um fundo (soft delete)",
        description = "Marca o fundo como inativo sem removê-lo do banco. Preserva o histórico."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Fundo desativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Fundo não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<Void> desativar(
            @Parameter(description = "ID do fundo", example = "1")
            @PathVariable Long id) {
        fundoService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
