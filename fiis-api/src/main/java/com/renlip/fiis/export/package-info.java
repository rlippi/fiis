/**
 * O pacote {@code com.renlip.fiis.export} concentra a geração de artefatos
 * exportáveis da carteira (PDF executivo e planilha XLSX consolidada).
 *
 * <p>É tratado como <i>cross-cutting concern</i> com pacote próprio
 * (paralelo a {@code audit/} e {@code metrics/}): cada classe aqui produz
 * um formato específico a partir de DTOs já calculados pelos services do
 * domínio. As classes deste pacote são <b>puras</b> — não acessam
 * repositórios nem o contexto de segurança; quem orquestra é o
 * {@code RelatorioExportService} em {@code service/}.</p>
 *
 * <p>Bibliotecas:
 * <ul>
 *   <li>{@code PdfExportSupport}: OpenPDF (fork moderno do iText 4);</li>
 *   <li>{@code XlsxExportSupport}: Apache POI XSSF.</li>
 * </ul>
 * </p>
 *
 * @author Renato Lippi
 * @since 1.0.0
 */
package com.renlip.fiis.export;
