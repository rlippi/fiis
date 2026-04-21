package com.renlip.fiis.domain.dto.brapi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Envelope retornado pela BRAPI no endpoint {@code /api/quote/{tickers}}.
 *
 * <p>Estrutura observada na resposta:</p>
 *
 * <pre>{@code
 * {
 *   "results": [ {...}, {...} ],
 *   "requestedAt": "2026-04-20T13:50:00.000Z",
 *   "took": "54ms"
 * }
 * }</pre>
 *
 * <p>Ignoramos {@code requestedAt} e {@code took} — só nos interessa a lista
 * de cotações em {@code results}.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BrapiQuoteResponse(
    List<BrapiQuote> results
) {
}
