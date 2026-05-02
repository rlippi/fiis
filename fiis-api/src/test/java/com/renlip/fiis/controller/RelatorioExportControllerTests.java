package com.renlip.fiis.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

/**
 * Testes de integração dos endpoints binários do {@code RelatorioController}.
 *
 * <p>Valida a exportação da posição consolidada em PDF e XLSX:
 * <ul>
 *   <li>headers HTTP corretos ({@code Content-Type} e
 *       {@code Content-Disposition: attachment});</li>
 *   <li>bytes não-vazios (PDF e XLSX nunca devem retornar arquivos truncados);</li>
 *   <li>conteúdo legível: PDF parseado pelo PDFBox e XLSX pelo POI,
 *       confirmando rótulos e tickers da fixture.</li>
 * </ul>
 * </p>
 *
 * <p>Reusa a mesma fixture do {@link RelatorioControllerTests} — 2 fundos
 * ativos (HGLG11, MXRF11) com operações, proventos e cotações.</p>
 */
@SqlGroup({
    @Sql(value = "/fixtures/setup.sql",                 executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/fixtures/relatorios/fii-script.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
})
class RelatorioExportControllerTests extends AbstractControllerTests {

    private static final String CONTENT_TYPE_XLSX =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Nested
    @DisplayName("GET /api/relatorios/posicao.pdf")
    class ExportarPdf {

        @Test
        @DisplayName("[200 OK] Retorna PDF com headers de download e conteúdo legível")
        void testExportarPdfComConteudoLegivel() {
            restTestClient.get("/api/relatorios/posicao.pdf")
                .expectStatus(HttpStatus.OK)
                .expectBody(byte[].class).consumeWith(response -> {
                    assertThat(response.getResponseHeaders().getContentType())
                        .isEqualTo(MediaType.APPLICATION_PDF);
                    assertThat(response.getResponseHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                        .contains("attachment", "fiis-posicao", ".pdf");

                    byte[] arquivo = response.getResponseBody();
                    assertThat(arquivo).isNotNull();
                    assertThat(arquivo.length).isGreaterThan(1000);

                    String texto = extrairTextoDoPdf(arquivo);
                    assertThat(texto).contains(
                        "Relatório de Carteira",
                        "Usuário de Teste",
                        "Indicadores",
                        "Alocação por Segmento",
                        "Posição Consolidada",
                        "HGLG11",
                        "MXRF11"
                    );
                });
        }

        private String extrairTextoDoPdf(byte[] arquivo) {
            try (PDDocument doc = Loader.loadPDF(arquivo)) {
                return new PDFTextStripper().getText(doc);
            } catch (IOException e) {
                throw new AssertionError("Falha ao parsear o PDF gerado", e);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/relatorios/posicao.xlsx")
    class ExportarXlsx {

        @Test
        @DisplayName("[200 OK] Retorna XLSX com aba 'Posição', tickers e linha de totais")
        void testExportarXlsxComEstruturaEsperada() {
            restTestClient.get("/api/relatorios/posicao.xlsx")
                .expectStatus(HttpStatus.OK)
                .expectBody(byte[].class).consumeWith(response -> {
                    String contentType = response.getResponseHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                    assertThat(contentType).isEqualTo(CONTENT_TYPE_XLSX);
                    assertThat(response.getResponseHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                        .contains("attachment", "fiis-posicao", ".xlsx");

                    byte[] arquivo = response.getResponseBody();
                    assertThat(arquivo).isNotNull();
                    assertThat(arquivo.length).isGreaterThan(1000);

                    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(arquivo))) {
                        XSSFSheet aba = workbook.getSheet("Posição");
                        assertThat(aba).as("Aba 'Posição' não encontrada no XLSX").isNotNull();

                        assertThat(aba.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Ticker");
                        assertThat(aba.getRow(0).getCell(1).getStringCellValue()).isEqualTo("Quantidade");

                        String tickerLinha1 = aba.getRow(1).getCell(0).getStringCellValue();
                        String tickerLinha2 = aba.getRow(2).getCell(0).getStringCellValue();
                        assertThat(List.of(tickerLinha1, tickerLinha2))
                            .containsExactlyInAnyOrder("HGLG11", "MXRF11");

                        assertThat(aba.getRow(3).getCell(0).getStringCellValue())
                            .as("Linha de totais ausente").isEqualTo("Total");
                    } catch (IOException e) {
                        throw new AssertionError("Falha ao parsear o XLSX gerado", e);
                    }
                });
        }
    }
}
