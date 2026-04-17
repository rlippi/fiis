package com.renlip.fiis.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.renlip.fiis.domain.dto.ProventoResponse;
import com.renlip.fiis.exception.ErroResponse;
import com.renlip.fiis.service.ProventoService;
import com.renlip.fiis.domain.vo.ProventoRequest;

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
 * Controller REST para proventos (rendimentos e amortizações) de FIIs.
 */
@RestController
@RequestMapping("/api/proventos")
@RequiredArgsConstructor
@Tag(name = "Proventos", description = "Rendimentos e amortizações pagos pelos FIIs")
public class ProventoController {

    private final ProventoService proventoService;

    /**
     * Lista proventos. Aceita filtros opcionais por fundo ou por intervalo
     * de data de pagamento.
     *
     * <p><b>Regras de combinação:</b>
     * <ul>
     *   <li>{@code fundoId} tem prioridade — se informado, ignora período;</li>
     *   <li>{@code inicio} e {@code fim} devem ser usados juntos;</li>
     *   <li>Sem filtros, retorna todos.</li>
     * </ul>
     * </p>
     *
     * @param fundoId ID do fundo (opcional)
     * @param inicio  data inicial de pagamento (opcional)
     * @param fim     data final de pagamento (opcional)
     * @return lista de proventos
     */
    @GetMapping
    @Operation(
        summary = "Lista proventos",
        description = "Lista todos ou filtra por fundo (fundoId) ou intervalo de pagamento (inicio/fim)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Fundo informado não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "409", description = "Intervalo de datas inválido",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<List<ProventoResponse>> listar(
            @Parameter(description = "Filtrar por ID do fundo")
            @RequestParam(required = false) Long fundoId,

            @Parameter(description = "Data inicial do pagamento (yyyy-MM-dd)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,

            @Parameter(description = "Data final do pagamento (yyyy-MM-dd)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        List<ProventoResponse> proventos;
        if (fundoId != null) {
            proventos = proventoService.listarPorFundo(fundoId);
        } else if (inicio != null && fim != null) {
            proventos = proventoService.listarPorPeriodoPagamento(inicio, fim);
        } else {
            proventos = proventoService.listarTodos();
        }
        return ResponseEntity.ok(proventos);
    }

    /**
     * Busca um provento pelo ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Busca provento por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Provento encontrado"),
        @ApiResponse(responseCode = "404", description = "Provento não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<ProventoResponse> buscarPorId(
            @Parameter(description = "ID do provento", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(proventoService.buscarPorId(id));
    }

    /**
     * Cria um novo provento.
     */
    @PostMapping
    @Operation(summary = "Cria um novo provento")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Provento criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "404", description = "Fundo não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "409", description = "Datas incoerentes (pagamento < referência)",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<ProventoResponse> criar(@Valid @RequestBody ProventoRequest request) {
        ProventoResponse criado = proventoService.criar(request);

        URI location = UriComponentsBuilder
            .fromPath("/api/proventos/{id}")
            .buildAndExpand(criado.id())
            .toUri();

        return ResponseEntity.created(location).body(criado);
    }

    /**
     * Atualiza um provento existente.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um provento existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Provento atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "404", description = "Provento ou fundo não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "409", description = "Datas incoerentes",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<ProventoResponse> atualizar(
            @Parameter(description = "ID do provento", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProventoRequest request) {
        return ResponseEntity.ok(proventoService.atualizar(id, request));
    }

    /**
     * Remove um provento (hard delete).
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um provento")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Provento removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Provento não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do provento", example = "1")
            @PathVariable Long id) {
        proventoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
