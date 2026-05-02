package com.renlip.fiis.support;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.renlip.fiis.config.BrapiProperties;
import com.renlip.fiis.domain.dto.brapi.BrapiQuote;
import com.renlip.fiis.domain.dto.brapi.BrapiQuoteResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

/**
 * Componente que executa <b>uma única chamada HTTP</b> à BRAPI para buscar
 * a cotação de um ticker, com tolerância a falhas via Resilience4j.
 *
 * <p><b>Por que classe separada do {@link BrapiClient}:</b> as annotations
 * {@code @Retry} e {@code @CircuitBreaker} só são interceptadas pelo proxy
 * AOP quando o método é chamado por outro bean. Se ficassem em
 * {@code BrapiClient}, o loop interno por ticker chamaria via {@code this.}
 * (self-invocation) e os aspectos não atuariam — exatamente a mesma
 * armadilha que fizemos no cache (#12).</p>
 *
 * <p><b>Patterns aplicados:</b>
 * <ul>
 *   <li>{@code @Retry}: refaz a chamada até 3x com backoff exponencial em
 *       caso de {@code HttpServerErrorException} (5xx) ou
 *       {@code ResourceAccessException} (timeouts/conexão recusada). 4xx
 *       não retenta — não vai melhorar refazendo;</li>
 *   <li>{@code @CircuitBreaker}: se 50% das últimas 10 chamadas falharem,
 *       o breaker abre por 30s para evitar martelar BRAPI fora do ar.</li>
 * </ul>
 *
 * <p>O timeout HTTP (connect + read) é configurado direto no {@code RestClient}
 * a partir de {@link BrapiProperties#timeoutMillis()} — alternativa mais simples
 * que adicionar {@code @TimeLimiter} (que exigiria método assíncrono).</p>
 */
@Component
@Slf4j
public class BrapiTickerFetcher {

    /**
     * Nome lógico das instâncias de Resilience4j (Retry e CircuitBreaker).
     * Configurações associadas em {@code application.yml} sob
     * {@code resilience4j.retry.instances.brapi} e
     * {@code resilience4j.circuitbreaker.instances.brapi}.
     */
    public static final String RESILIENCE_INSTANCE = "brapi";

    private final BrapiProperties properties;

    private final RestClient restClient;

    public BrapiTickerFetcher(final BrapiProperties properties) {
        this.properties = properties;
        ClientHttpRequestFactory factory = ClientHttpRequestFactories.get(
            ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(properties.timeoutMillis()))
                .withReadTimeout(Duration.ofMillis(properties.timeoutMillis()))
        );
        this.restClient = RestClient.builder()
            .baseUrl(properties.url())
            .requestFactory(factory)
            .build();
    }

    /**
     * Busca a cotação de um único ticker na BRAPI.
     *
     * @param ticker código de negociação (ex: {@code "HGLG11"})
     * @return cotação encontrada, ou {@link Optional#empty()} quando o ticker
     *         não está disponível (4xx — não existe, formato inválido, sem
     *         quota etc.) ou quando a resposta vem sem {@code results}
     * @throws org.springframework.web.client.RestClientException quando a BRAPI
     *         falha de forma transiente (5xx/timeout) e o {@code @Retry} não
     *         conseguiu recuperar, OU quando o circuit breaker está aberto
     *         (lança {@code CallNotPermittedException} que estende
     *         {@code RuntimeException}, capturada pelo {@code BrapiClient})
     */
    @Retry(name = RESILIENCE_INSTANCE)
    @CircuitBreaker(name = RESILIENCE_INSTANCE)
    public Optional<BrapiQuote> buscarTicker(final String ticker) {
        try {
            BrapiQuoteResponse resposta = restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/quote/{ticker}");
                    if (StringUtils.hasText(properties.token())) {
                        uriBuilder.queryParam("token", properties.token());
                    }
                    return uriBuilder.build(ticker);
                })
                .retrieve()
                .body(BrapiQuoteResponse.class);

            if (resposta == null || resposta.results() == null) {
                return Optional.empty();
            }
            List<BrapiQuote> results = resposta.results();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (HttpClientErrorException ex) {
            // 4xx é "esse ticker não está disponível agora" — não é falha de
            // infraestrutura, então não retentamos nem contamos para o circuit
            // breaker. Retornamos Optional.empty() e o caller reporta em
            // naoEncontradosBrapi.
            log.warn("BRAPI rejeitou ticker {} com status {}: {}",
                ticker, ex.getStatusCode(), ex.getResponseBodyAsString());
            return Optional.empty();
        }
    }
}
