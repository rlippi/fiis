package com.renlip.fiis.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.renlip.fiis.domain.dto.AlocacaoResponse;
import com.renlip.fiis.domain.dto.RendaMensalResponse;
import com.renlip.fiis.domain.dto.RendaPorFundoResponse;
import com.renlip.fiis.domain.dto.ResumoCarteiraResponse;
import com.renlip.fiis.service.RelatorioExportService;
import com.renlip.fiis.service.RelatorioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controller REST de relatórios e agregações da carteira.
 *
 * <p>Todos os endpoints são de leitura — alimentam o dashboard do frontend.</p>
 */
@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Relatórios agregados da carteira (dashboard)")
public class RelatorioController {

    private static final String CONTENT_TYPE_XLSX =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final RelatorioService relatorioService;
    private final RelatorioExportService relatorioExportService;

    /**
     * Renda passiva agrupada por mês de pagamento.
     *
     * @return lista ordenada do mês mais recente ao mais antigo
     */
    @GetMapping("/renda-mensal")
    @Operation(
        summary = "Renda passiva por mês",
        description = "Agrega todos os proventos por mês de pagamento, ordenando do mais recente ao mais antigo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    })
    public ResponseEntity<List<RendaMensalResponse>> rendaMensal() {
        return ResponseEntity.ok(relatorioService.gerarRendaMensal());
    }

    /**
     * Renda passiva agrupada por fundo, ordenada pelo maior recebedor.
     *
     * @return lista de renda por fundo
     */
    @GetMapping("/renda-por-fundo")
    @Operation(
        summary = "Renda passiva por fundo",
        description = "Soma os proventos recebidos por fundo, ordenando do maior ao menor recebedor."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    })
    public ResponseEntity<List<RendaPorFundoResponse>> rendaPorFundo() {
        return ResponseEntity.ok(relatorioService.gerarRendaPorFundo());
    }

    /**
     * Alocação da carteira por tipo de fundo (TIJOLO, PAPEL, etc.).
     *
     * @return lista ordenada do maior custo ao menor
     */
    @GetMapping("/alocacao-por-tipo")
    @Operation(
        summary = "Alocação por tipo de fundo",
        description = "Distribui o custo atual da carteira por tipo (Tijolo, Papel, Híbrido, FoF)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    })
    public ResponseEntity<List<AlocacaoResponse>> alocacaoPorTipo() {
        return ResponseEntity.ok(relatorioService.gerarAlocacaoPorTipo());
    }

    /**
     * Alocação da carteira por segmento de atuação (LOGISTICA, SHOPPING, etc.).
     *
     * @return lista ordenada do maior custo ao menor
     */
    @GetMapping("/alocacao-por-segmento")
    @Operation(
        summary = "Alocação por segmento",
        description = "Distribui o custo atual da carteira por segmento (Logística, Shopping, Lajes, etc.)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    })
    public ResponseEntity<List<AlocacaoResponse>> alocacaoPorSegmento() {
        return ResponseEntity.ok(relatorioService.gerarAlocacaoPorSegmento());
    }

    /**
     * Resumo geral da carteira com totais consolidados.
     *
     * <p>Ideal como dado inicial do dashboard — traz em uma única chamada
     * todos os indicadores principais (custo, proventos, yield, lucro, etc.).</p>
     *
     * @return resumo da carteira
     */
    @GetMapping("/resumo-carteira")
    @Operation(
        summary = "Resumo geral da carteira",
        description = "Retorna totais consolidados: custo, proventos, yield, lucro realizado, média mensal."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resumo gerado com sucesso")
    })
    public ResponseEntity<ResumoCarteiraResponse> resumoCarteira() {
        return ResponseEntity.ok(relatorioService.gerarResumoCarteira());
    }

    /**
     * Exporta a posição consolidada da carteira em PDF (1 página executiva).
     */
    @GetMapping(value = "/posicao.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
        summary = "Exporta a posição da carteira em PDF",
        description = "Gera um relatório executivo de uma página com KPIs, alocação por segmento e posição consolidada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso")
    })
    public ResponseEntity<byte[]> exportarPosicaoPdf() {
        byte[] arquivo = relatorioExportService.exportarPosicaoPdf();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, dispositionAttachment("fiis-posicao", "pdf"))
            .body(arquivo);
    }

    /**
     * Exporta a posição consolidada da carteira em XLSX (planilha analítica).
     */
    @GetMapping(value = "/posicao.xlsx", produces = CONTENT_TYPE_XLSX)
    @Operation(
        summary = "Exporta a posição da carteira em XLSX",
        description = "Gera uma planilha Excel com a posição consolidada (uma linha por fundo) e linha de totais."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "XLSX gerado com sucesso")
    })
    public ResponseEntity<byte[]> exportarPosicaoXlsx() {
        byte[] arquivo = relatorioExportService.exportarPosicaoXlsx();
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(CONTENT_TYPE_XLSX))
            .header(HttpHeaders.CONTENT_DISPOSITION, dispositionAttachment("fiis-posicao", "xlsx"))
            .body(arquivo);
    }

    private static String dispositionAttachment(String prefixoNome, String extensao) {
        return "attachment; filename=\"" + prefixoNome + "-" + LocalDate.now() + "." + extensao + "\"";
    }
}
