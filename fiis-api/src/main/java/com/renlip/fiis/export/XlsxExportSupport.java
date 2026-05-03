package com.renlip.fiis.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.renlip.fiis.domain.dto.PosicaoResponse;

/**
 * Gerador da planilha XLSX consolidada da carteira (aba {@code Posição}).
 *
 * <p>Recebe a lista de {@link PosicaoResponse} já calculada e devolve o
 * arquivo como {@code byte[]}. Não acessa repositórios nem segurança.</p>
 */
@Component
public class XlsxExportSupport {

    private static final String NOME_ABA = "Posição";

    private static final String[] COLUNAS = {
        "Ticker",
        "Quantidade",
        "Preço médio",
        "Custo total",
        "Última cotação",
        "Valor atual",
        "Variação R$",
        "Variação %",
        "Renda total",
        "Yield on cost %"
    };

    public byte[] gerarPosicaoConsolidada(List<PosicaoResponse> posicoes) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(NOME_ABA);

            EstilosCelulas estilos = new EstilosCelulas(workbook);

            preencherCabecalho(sheet, estilos);
            preencherLinhasDePosicao(sheet, posicoes, estilos);
            if (!posicoes.isEmpty()) {
                preencherLinhaDeTotais(sheet, posicoes, estilos);
            }

            sheet.createFreezePane(0, 1);
            for (int i = 0; i < COLUNAS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gerar XLSX de posição da carteira", e);
        }
    }

    private void preencherCabecalho(Sheet sheet, EstilosCelulas estilos) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < COLUNAS.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(COLUNAS[i]);
            cell.setCellStyle(estilos.cabecalho);
        }
    }

    private void preencherLinhasDePosicao(Sheet sheet, List<PosicaoResponse> posicoes, EstilosCelulas estilos) {
        int linha = 1;
        for (PosicaoResponse p : posicoes) {
            Row row = sheet.createRow(linha++);
            preencherCelulaTexto(row, 0, p.fundo().ticker(), estilos.texto);
            preencherCelulaInteiro(row, 1, p.quantidadeCotas(), estilos.inteiro);
            preencherCelulaMoeda(row, 2, p.precoMedio(), estilos.moeda);
            preencherCelulaMoeda(row, 3, p.custoAtual(), estilos.moeda);
            preencherCelulaMoeda(row, 4, p.precoAtual(), estilos.moeda);
            preencherCelulaMoeda(row, 5, p.valorAtual(), estilos.moeda);
            preencherCelulaMoeda(row, 6, calcularVariacaoMonetaria(p), estilos.moeda);
            preencherCelulaPercentual(row, 7, p.variacaoPercentual(), estilos.percentual);
            preencherCelulaMoeda(row, 8, p.totalProventos(), estilos.moeda);
            preencherCelulaPercentual(row, 9, p.yieldSobreCustoPercentual(), estilos.percentual);
        }
    }

    private void preencherLinhaDeTotais(Sheet sheet, List<PosicaoResponse> posicoes, EstilosCelulas estilos) {
        BigDecimal totalCusto = somar(posicoes, PosicaoResponse::custoAtual);
        BigDecimal totalValor = somar(posicoes, PosicaoResponse::valorAtual);
        BigDecimal totalRenda = somar(posicoes, PosicaoResponse::totalProventos);
        BigDecimal totalVariacao = totalValor.subtract(totalCusto);

        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        Cell rotulo = row.createCell(0);
        rotulo.setCellValue("Total");
        rotulo.setCellStyle(estilos.totalTexto);

        for (int i = 1; i <= 2; i++) {
            row.createCell(i).setCellStyle(estilos.totalTexto);
        }
        preencherCelulaMoeda(row, 3, totalCusto, estilos.totalMoeda);
        for (int i = 4; i <= 4; i++) {
            row.createCell(i).setCellStyle(estilos.totalTexto);
        }
        preencherCelulaMoeda(row, 5, totalValor, estilos.totalMoeda);
        preencherCelulaMoeda(row, 6, totalVariacao, estilos.totalMoeda);
        row.createCell(7).setCellStyle(estilos.totalTexto);
        preencherCelulaMoeda(row, 8, totalRenda, estilos.totalMoeda);
        row.createCell(9).setCellStyle(estilos.totalTexto);
    }

    private static BigDecimal calcularVariacaoMonetaria(PosicaoResponse p) {
        return p.valorAtual() != null && p.custoAtual() != null
            ? p.valorAtual().subtract(p.custoAtual())
            : null;
    }

    private static BigDecimal somar(List<PosicaoResponse> posicoes,
                                    java.util.function.Function<PosicaoResponse, BigDecimal> extrator) {
        return posicoes.stream()
            .map(extrator)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static void preencherCelulaTexto(Row row, int col, String valor, CellStyle estilo) {
        Cell cell = row.createCell(col);
        if (valor != null) {
            cell.setCellValue(valor);
        }
        cell.setCellStyle(estilo);
    }

    private static void preencherCelulaInteiro(Row row, int col, Integer valor, CellStyle estilo) {
        Cell cell = row.createCell(col);
        if (valor != null) {
            cell.setCellValue(valor);
        }
        cell.setCellStyle(estilo);
    }

    private static void preencherCelulaMoeda(Row row, int col, BigDecimal valor, CellStyle estilo) {
        Cell cell = row.createCell(col);
        if (valor != null) {
            cell.setCellValue(valor.doubleValue());
        }
        cell.setCellStyle(estilo);
    }

    /**
     * O serviço retorna percentuais já em escala "humana" (ex: 4.34 = 4,34%).
     * O formato XLSX {@code 0.00%} multiplica o valor da célula por 100 ao
     * exibir — por isso dividimos por 100 antes de gravar.
     */
    private static void preencherCelulaPercentual(Row row, int col, BigDecimal valorEmEscalaHumana, CellStyle estilo) {
        Cell cell = row.createCell(col);
        if (valorEmEscalaHumana != null) {
            BigDecimal fracao = valorEmEscalaHumana.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
            cell.setCellValue(fracao.doubleValue());
        }
        cell.setCellStyle(estilo);
    }

    private static final class EstilosCelulas {
        final CellStyle cabecalho;
        final CellStyle texto;
        final CellStyle inteiro;
        final CellStyle moeda;
        final CellStyle percentual;
        final CellStyle totalTexto;
        final CellStyle totalMoeda;

        EstilosCelulas(Workbook wb) {
            short formatoMoeda = wb.createDataFormat().getFormat("\"R$\" #,##0.00");
            short formatoInteiro = wb.createDataFormat().getFormat("#,##0");
            short formatoPercentual = wb.createDataFormat().getFormat("0.00%");

            Font fonteNegrito = wb.createFont();
            fonteNegrito.setBold(true);

            this.cabecalho = wb.createCellStyle();
            this.cabecalho.setFont(fonteNegrito);
            this.cabecalho.setAlignment(HorizontalAlignment.CENTER);
            this.cabecalho.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            this.cabecalho.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            aplicarBordasFinas(this.cabecalho);

            this.texto = wb.createCellStyle();
            aplicarBordasFinas(this.texto);

            this.inteiro = wb.createCellStyle();
            this.inteiro.setDataFormat(formatoInteiro);
            this.inteiro.setAlignment(HorizontalAlignment.RIGHT);
            aplicarBordasFinas(this.inteiro);

            this.moeda = wb.createCellStyle();
            this.moeda.setDataFormat(formatoMoeda);
            this.moeda.setAlignment(HorizontalAlignment.RIGHT);
            aplicarBordasFinas(this.moeda);

            this.percentual = wb.createCellStyle();
            this.percentual.setDataFormat(formatoPercentual);
            this.percentual.setAlignment(HorizontalAlignment.RIGHT);
            aplicarBordasFinas(this.percentual);

            this.totalTexto = wb.createCellStyle();
            this.totalTexto.setFont(fonteNegrito);
            this.totalTexto.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            this.totalTexto.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            aplicarBordasFinas(this.totalTexto);

            this.totalMoeda = wb.createCellStyle();
            this.totalMoeda.setFont(fonteNegrito);
            this.totalMoeda.setDataFormat(formatoMoeda);
            this.totalMoeda.setAlignment(HorizontalAlignment.RIGHT);
            this.totalMoeda.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            this.totalMoeda.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            aplicarBordasFinas(this.totalMoeda);
        }

        private static void aplicarBordasFinas(CellStyle estilo) {
            estilo.setBorderTop(BorderStyle.THIN);
            estilo.setBorderBottom(BorderStyle.THIN);
            estilo.setBorderLeft(BorderStyle.THIN);
            estilo.setBorderRight(BorderStyle.THIN);
        }
    }
}
