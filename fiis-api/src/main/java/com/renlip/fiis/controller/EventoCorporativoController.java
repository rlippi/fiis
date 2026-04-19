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

import com.renlip.fiis.domain.dto.EventoCorporativoResponse;
import com.renlip.fiis.exception.ErroResponse;
import com.renlip.fiis.service.EventoCorporativoService;
import com.renlip.fiis.domain.vo.EventoCorporativoRequest;

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
 * Controller REST para eventos corporativos dos FIIs (bonificação,
 * desdobramento, grupamento).
 */
@RestController
@RequestMapping("/api/eventos-corporativos")
@RequiredArgsConstructor
@Tag(name = "Eventos Corporativos", description = "Bonificações, desdobramentos e grupamentos dos FIIs")
public class EventoCorporativoController {

    private final EventoCorporativoService eventoService;

    /**
     * Lista eventos. Se {@code fundoId} for informado, filtra pelo fundo.
     *
     * @param fundoId opcional — ID do fundo para filtrar
     * @return lista de eventos
     */
    @GetMapping
    @Operation(
        summary = "Lista eventos corporativos",
        description = "Retorna todos os eventos cadastrados. Use fundoId para filtrar por fundo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Fundo informado não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<List<EventoCorporativoResponse>> listar(
            @Parameter(description = "Filtrar por ID do fundo")
            @RequestParam(required = false) Long fundoId) {

        List<EventoCorporativoResponse> eventos = fundoId != null
            ? eventoService.listarPorFundo(fundoId)
            : eventoService.listarTodos();

        return ResponseEntity.ok(eventos);
    }

    /**
     * Busca um evento pelo ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Busca evento por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento encontrado"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<EventoCorporativoResponse> buscarPorId(
            @Parameter(description = "ID do evento", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    /**
     * Cria um novo evento corporativo.
     */
    @PostMapping
    @Operation(summary = "Cria um novo evento corporativo")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Evento criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "404", description = "Fundo não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<EventoCorporativoResponse> criar(@Valid @RequestBody EventoCorporativoRequest request) {
        EventoCorporativoResponse criado = eventoService.criar(request);

        URI location = UriComponentsBuilder
            .fromPath("/api/eventos-corporativos/{id}")
            .buildAndExpand(criado.id())
            .toUri();

        return ResponseEntity.created(location).body(criado);
    }

    /**
     * Atualiza um evento existente.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um evento existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "404", description = "Evento ou fundo não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<EventoCorporativoResponse> atualizar(
            @Parameter(description = "ID do evento", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody EventoCorporativoRequest request) {
        return ResponseEntity.ok(eventoService.atualizar(id, request));
    }

    /**
     * Remove um evento (hard delete).
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um evento corporativo")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Evento removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do evento", example = "1")
            @PathVariable Long id) {
        eventoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
