package com.renlip.fiis.export;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.renlip.fiis.domain.dto.AlocacaoResponse;
import com.renlip.fiis.domain.dto.PosicaoResponse;
import com.renlip.fiis.domain.dto.ResumoCarteiraResponse;

/**
 * Gerador do PDF executivo da carteira (1 página A4).
 *
 * <p>É uma classe pura — recebe um {@link Dados} já calculado e devolve
 * o PDF como {@code byte[]}. Não conhece repositórios, segurança ou HTTP.</p>
 */
@Component
public class PdfExportSupport {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(PT_BR);
    private static final DecimalFormat PERCENTUAL = new DecimalFormat("0.00'%'", new DecimalFormatSymbols(PT_BR));
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);

    private static final Font FONTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
    private static final Font FONTE_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
    private static final Font FONTE_SECAO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font FONTE_CABECALHO_TABELA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font FONTE_CORPO = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font FONTE_RODAPE = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

    private static final Color COR_CABECALHO_TABELA = new Color(230, 230, 230);

    /**
     * Insumos para gerar o PDF executivo.
     * Modelado como nested record para deixar explícito que pertence ao gerador.
     */
    public record Dados(
        String nomeUsuario,
        LocalDate dataEmissao,
        ResumoCarteiraResponse resumo,
        List<AlocacaoResponse> alocacaoPorSegmento,
        List<PosicaoResponse> posicoes
    ) {}

    public byte[] gerarPosicaoExecutiva(Dados dados) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(documento, baos);
            documento.open();

            adicionarCabecalho(documento, dados);
            adicionarKpis(documento, dados.resumo());
            adicionarAlocacaoPorSegmento(documento, dados.alocacaoPorSegmento());
            adicionarPosicaoConsolidada(documento, dados.posicoes());
            adicionarRodape(documento);

            documento.close();
            return baos.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new IllegalStateException("Falha ao gerar PDF de posição da carteira", e);
        }
    }

    private void adicionarCabecalho(Document doc, Dados dados) throws DocumentException {
        Paragraph titulo = new Paragraph("Relatório de Carteira", FONTE_TITULO);
        titulo.setAlignment(Element.ALIGN_LEFT);
        doc.add(titulo);

        String linhaSubtitulo = String.format(
            "Investidor: %s    |    Emitido em: %s",
            dados.nomeUsuario(),
            dados.dataEmissao().format(DATA_BR)
        );
        Paragraph subtitulo = new Paragraph(linhaSubtitulo, FONTE_SUBTITULO);
        subtitulo.setSpacingAfter(14f);
        doc.add(subtitulo);
    }

    private void adicionarKpis(Document doc, ResumoCarteiraResponse resumo) throws DocumentException {
        doc.add(secao("Indicadores"));

        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100f);
        tabela.setSpacingBefore(4f);
        tabela.setSpacingAfter(12f);

        tabela.addCell(celulaCabecalho("Patrimônio"));
        tabela.addCell(celulaCabecalho("Custo total"));
        tabela.addCell(celulaCabecalho("Variação patrimonial"));
        tabela.addCell(celulaCabecalho("Renda total"));

        tabela.addCell(celulaTextoCentralizado(formatarMoeda(resumo.valorTotalCarteira())));
        tabela.addCell(celulaTextoCentralizado(formatarMoeda(resumo.custoTotalCarteira())));
        tabela.addCell(celulaTextoCentralizado(formatarPercentual(resumo.variacaoPatrimonialPercentual())));
        tabela.addCell(celulaTextoCentralizado(formatarMoeda(resumo.totalProventosRecebidos())));

        doc.add(tabela);
    }

    private void adicionarAlocacaoPorSegmento(Document doc, List<AlocacaoResponse> alocacoes) throws DocumentException {
        doc.add(secao("Alocação por Segmento"));

        PdfPTable tabela = new PdfPTable(new float[] { 3f, 2f, 1.5f });
        tabela.setWidthPercentage(100f);
        tabela.setSpacingBefore(4f);
        tabela.setSpacingAfter(12f);

        tabela.addCell(celulaCabecalho("Segmento"));
        tabela.addCell(celulaCabecalho("Custo (R$)"));
        tabela.addCell(celulaCabecalho("%"));

        if (alocacoes.isEmpty()) {
            PdfPCell vazia = celulaTexto("Sem fundos cadastrados");
            vazia.setColspan(3);
            vazia.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabela.addCell(vazia);
        } else {
            for (AlocacaoResponse a : alocacoes) {
                tabela.addCell(celulaTexto(a.categoriaDescricao()));
                tabela.addCell(celulaNumero(formatarMoeda(a.custoAtual())));
                tabela.addCell(celulaNumero(formatarPercentual(a.percentual())));
            }
        }

        doc.add(tabela);
    }

    private void adicionarPosicaoConsolidada(Document doc, List<PosicaoResponse> posicoes) throws DocumentException {
        doc.add(secao("Posição Consolidada"));

        PdfPTable tabela = new PdfPTable(new float[] { 1.2f, 0.8f, 1.2f, 1.4f, 1f });
        tabela.setWidthPercentage(100f);
        tabela.setSpacingBefore(4f);

        tabela.addCell(celulaCabecalho("Ticker"));
        tabela.addCell(celulaCabecalho("Qty"));
        tabela.addCell(celulaCabecalho("PM"));
        tabela.addCell(celulaCabecalho("Valor atual"));
        tabela.addCell(celulaCabecalho("Variação"));

        if (posicoes.isEmpty()) {
            PdfPCell vazia = celulaTexto("Nenhuma posição em carteira");
            vazia.setColspan(5);
            vazia.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabela.addCell(vazia);
        } else {
            for (PosicaoResponse p : posicoes) {
                tabela.addCell(celulaTexto(p.fundo().ticker()));
                tabela.addCell(celulaNumero(String.valueOf(p.quantidadeCotas())));
                tabela.addCell(celulaNumero(formatarMoeda(p.precoMedio())));
                tabela.addCell(celulaNumero(formatarMoeda(p.valorAtual())));
                tabela.addCell(celulaNumero(formatarPercentual(p.variacaoPercentual())));
            }
        }

        doc.add(tabela);
    }

    private void adicionarRodape(Document doc) throws DocumentException {
        Paragraph rodape = new Paragraph("Gerado por fiis.app", FONTE_RODAPE);
        rodape.setAlignment(Element.ALIGN_CENTER);
        rodape.setSpacingBefore(20f);
        doc.add(rodape);
    }

    private static Paragraph secao(String titulo) {
        Paragraph p = new Paragraph(titulo, FONTE_SECAO);
        p.setSpacingBefore(6f);
        return p;
    }

    private static PdfPCell celulaCabecalho(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONTE_CABECALHO_TABELA));
        cell.setBackgroundColor(COR_CABECALHO_TABELA);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        return cell;
    }

    private static PdfPCell celulaTexto(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONTE_CORPO));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(5f);
        return cell;
    }

    private static PdfPCell celulaTextoCentralizado(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONTE_CORPO));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        return cell;
    }

    private static PdfPCell celulaNumero(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONTE_CORPO));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(5f);
        return cell;
    }

    private static String formatarMoeda(BigDecimal valor) {
        return valor != null ? MOEDA.format(valor) : "—";
    }

    private static String formatarPercentual(BigDecimal valor) {
        return valor != null ? PERCENTUAL.format(valor) : "—";
    }
}
