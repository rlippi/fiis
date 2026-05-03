package com.renlip.fiis.domain.dto.brapi;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Cotação de um ticker devolvida pela BRAPI no endpoint
 * {@code /api/quote/{tickers}}.
 *
 * <p>A BRAPI retorna dezenas de campos por cotação (variações percentuais,
 * médias de volume, P/VP, 52-week range, etc). Mapeamos apenas os que
 * correspondem à nossa entidade {@code Cotacao}; o restante é ignorado via
 * {@link JsonIgnoreProperties}.</p>
 *
 * <p>O campo {@code regularMarketPrice} representa o último preço negociado
 * no pregão atual e é usado como {@code precoFechamento} em nosso domínio.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BrapiQuote(
    String symbol,
    BigDecimal regularMarketPrice,
    BigDecimal regularMarketOpen,
    BigDecimal regularMarketDayLow,
    BigDecimal regularMarketDayHigh,
    BigDecimal regularMarketVolume
) {
}
