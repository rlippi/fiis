package com.renlip.fiis.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.domain.dto.AlocacaoResponse;
import com.renlip.fiis.domain.dto.PosicaoResponse;
import com.renlip.fiis.export.PdfExportSupport;
import com.renlip.fiis.export.XlsxExportSupport;
import com.renlip.fiis.support.UsuarioLogadoSupport;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável por orquestrar a exportação dos relatórios da carteira
 * em formatos binários (PDF e XLSX).
 *
 * <p>É o ponto de entrada transacional: faz o lookup pelos services de domínio
 * (cuja consulta já filtra por usuário autenticado), monta os insumos e
 * delega a geração binária aos {@code Support} do pacote {@code export/}.</p>
 *
 * <p><b>Multi-usuário:</b> nenhum filtro de usuário é aplicado aqui — os
 * services delegados ({@link RelatorioService}, {@link PosicaoService}) já
 * restringem a consulta ao usuário autenticado, mantendo a invariante da
 * carteira por usuário.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatorioExportService {

    private final RelatorioService relatorioService;
    private final PosicaoService posicaoService;
    private final UsuarioLogadoSupport usuarioLogado;
    private final PdfExportSupport pdfExportSupport;
    private final XlsxExportSupport xlsxExportSupport;

    public byte[] exportarPosicaoPdf() {
        PdfExportSupport.Dados dados = new PdfExportSupport.Dados(
            usuarioLogado.getUsuarioAtual().getNome(),
            LocalDate.now(),
            relatorioService.gerarResumoCarteira(),
            alocacaoComCustoEmCarteira(),
            posicoesComCotasEmCarteira()
        );
        return pdfExportSupport.gerarPosicaoExecutiva(dados);
    }

    public byte[] exportarPosicaoXlsx() {
        return xlsxExportSupport.gerarPosicaoConsolidada(posicoesComCotasEmCarteira());
    }

    private List<PosicaoResponse> posicoesComCotasEmCarteira() {
        return posicaoService.calcularPosicaoDeTodos().stream()
            .filter(p -> p.quantidadeCotas() > 0)
            .toList();
    }

    private List<AlocacaoResponse> alocacaoComCustoEmCarteira() {
        return relatorioService.gerarAlocacaoPorSegmento().stream()
            .filter(a -> a.custoAtual().signum() > 0)
            .toList();
    }
}
