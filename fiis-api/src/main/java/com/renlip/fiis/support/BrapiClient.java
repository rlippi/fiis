package com.renlip.fiis.support;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.renlip.fiis.domain.dto.brapi.BrapiQuote;
import com.renlip.fiis.domain.dto.brapi.BrapiQuoteResponse;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;

/**
 * Cliente HTTP para a BRAPI (https://brapi.dev) — orquestra a busca de
 * múltiplos tickers em uma única chamada lógica.
 *
 * <p>Esta classe não faz a chamada HTTP em si; isso fica a cargo de
 * {@link BrapiTickerFetcher}, que aplica Resilience4j por chamada individual.
 * Aqui apenas iteramos sobre os tickers solicitados, agregamos os resultados
 * encontrados e traduzimos o caso "circuit breaker aberto" para
 * {@link RestClientException}, mantendo o contrato externo único:
 * <i>tudo o que falha de forma irrecuperável vira {@code RestClientException}</i>.</p>
 *
 * <p><b>Free tier da BRAPI:</b> 1 ativo por requisição. Carteiras com múltiplos
 * fundos resultam em N chamadas HTTP — cada uma protegida individualmente
 * pelo Retry e contribuindo para a janela do circuit breaker.</p>
 */
@Component
@RequiredArgsConstructor
public class BrapiClient {

    private final BrapiTickerFetcher tickerFetcher;

    /**
     * Busca na BRAPI a cotação atual de múltiplos tickers.
     *
     * <p>Cada ticker é buscado individualmente via {@link BrapiTickerFetcher},
     * que aplica Retry e CircuitBreaker. Tickers ausentes (4xx) já viram
     * {@link Optional#empty()} no fetcher e não aparecem no resultado — o
     * caller compara com a lista solicitada para identificar os que faltaram.</p>
     *
     * @param tickers lista de códigos de negociação (ex: {@code ["HGLG11", "KNCR11"]})
     * @return envelope com as cotações encontradas (subset dos tickers solicitados)
     * @throws RestClientException quando o circuit breaker bloqueou a chamada
     *         (BRAPI está sendo identificada como indisponível) ou quando uma
     *         falha transitória 5xx/timeout esgotou o retry
     */
    public BrapiQuoteResponse buscarCotacoes(final List<String> tickers) {
        try {
            List<BrapiQuote> encontradas = tickers.stream()
                .map(tickerFetcher::buscarTicker)
                .flatMap(Optional::stream)
                .toList();
            return new BrapiQuoteResponse(encontradas);
        } catch (CallNotPermittedException ex) {
            // CB aberto: traduz para a mesma RestClientException que sinaliza
            // BRAPI indisponível, mantendo o contrato consumido pelo
            // CotacaoService (que mapeia para COTACAO_BRAPI_INDISPONIVEL).
            throw new RestClientException(
                "Circuit breaker aberto para BRAPI: " + ex.getMessage(), ex);
        }
    }
}
