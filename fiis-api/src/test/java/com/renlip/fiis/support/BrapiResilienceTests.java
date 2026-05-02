package com.renlip.fiis.support;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClientException;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.renlip.fiis.domain.dto.brapi.BrapiQuote;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

/**
 * Testes de integração da resiliência da camada BRAPI.
 *
 * <p>Sobe um {@link WireMockServer} em porta dinâmica antes do contexto Spring
 * carregar; o lambda do {@link DynamicPropertySource} aponta {@code brapi.url}
 * para esse servidor. Assim, o {@code BrapiTickerFetcher} construído pelo Spring
 * já fala com o WireMock.</p>
 *
 * <p>O profile {@code test} reduz o backoff do Retry para 1ms (ver
 * {@code application-test.properties}), evitando que cada teste de retry exhaust
 * gaste segundos esperando o backoff.</p>
 *
 * <p>Cada {@code @Test} reseta o estado do WireMock (stubs e contagem) e do
 * {@link CircuitBreaker} para impedir contaminação entre cenários.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class BrapiResilienceTests {

    private static final WireMockServer WIREMOCK = new WireMockServer(options().dynamicPort());

    static {
        WIREMOCK.start();
        Runtime.getRuntime().addShutdownHook(new Thread(WIREMOCK::stop));
    }

    @DynamicPropertySource
    static void brapiUrl(final DynamicPropertyRegistry registry) {
        registry.add("brapi.url", () -> "http://localhost:" + WIREMOCK.port());
    }

    @Autowired
    private BrapiTickerFetcher fetcher;

    @Autowired
    private BrapiClient brapiClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetState() {
        WIREMOCK.resetAll();
        circuitBreakerRegistry.circuitBreaker(BrapiTickerFetcher.RESILIENCE_INSTANCE).reset();
    }

    private static final String OK_RESPONSE = """
        {"results":[
          {"symbol":"HGLG11","regularMarketPrice":160.25,"regularMarketOpen":158.90,
           "regularMarketDayLow":158.00,"regularMarketDayHigh":161.50,"regularMarketVolume":1250000.00}
        ]}
        """;

    @Nested
    @DisplayName("@Retry")
    class RetryTests {

        @Test
        @DisplayName("Refaz a chamada em 5xx e tem sucesso na 2ª tentativa")
        void testRetrySucesso() {
            WIREMOCK.stubFor(get(urlPathEqualTo("/api/quote/HGLG11"))
                .inScenario("retry-sucesso")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("ok"));
            WIREMOCK.stubFor(get(urlPathEqualTo("/api/quote/HGLG11"))
                .inScenario("retry-sucesso")
                .whenScenarioStateIs("ok")
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(OK_RESPONSE)));

            Optional<BrapiQuote> result = fetcher.buscarTicker("HGLG11");

            assertThat(result).isPresent();
            assertThat(result.get().symbol()).isEqualTo("HGLG11");
            WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo("/api/quote/HGLG11")));
        }

        @Test
        @DisplayName("Esgota as 3 tentativas em 5xx persistente e propaga RestClientException")
        void testRetryEsgota() {
            WIREMOCK.stubFor(get(urlPathEqualTo("/api/quote/HGLG11"))
                .willReturn(aResponse().withStatus(503)));

            assertThatThrownBy(() -> brapiClient.buscarCotacoes(List.of("HGLG11")))
                .isInstanceOf(RestClientException.class);

            WIREMOCK.verify(3, getRequestedFor(urlPathEqualTo("/api/quote/HGLG11")));
        }

        @Test
        @DisplayName("4xx não retenta — única chamada e retorno vazio")
        void test4xxNaoRetenta() {
            WIREMOCK.stubFor(get(urlPathEqualTo("/api/quote/INEXISTENT"))
                .willReturn(aResponse().withStatus(404)));

            Optional<BrapiQuote> result = fetcher.buscarTicker("INEXISTENT");

            assertThat(result).isEmpty();
            WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/api/quote/INEXISTENT")));
        }
    }

    @Nested
    @DisplayName("@CircuitBreaker")
    class CircuitBreakerTests {

        @Test
        @DisplayName("Abre após acumular falhas e bloqueia chamadas seguintes sem ir ao HTTP")
        void testCircuitBreakerAbre() {
            WIREMOCK.stubFor(get(urlPathEqualTo("/api/quote/HGLG11"))
                .willReturn(aResponse().withStatus(503)));

            // CB config: sliding-window=10, min-calls=5, threshold=50%.
            // Cada chamada lógica gera até 3 HTTP (Retry max-attempts=3); 3 chamadas
            // lógicas falhando = ~9 HTTP, mais que suficiente para abrir o CB.
            for (int i = 0; i < 3; i++) {
                try {
                    fetcher.buscarTicker("HGLG11");
                } catch (Exception ignored) {
                    // exception esperada (HttpServerErrorException ou CallNotPermittedException)
                }
            }

            CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(BrapiTickerFetcher.RESILIENCE_INSTANCE);
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

            WIREMOCK.resetRequests();

            // Com CB aberto, BrapiClient.buscarCotacoes captura CallNotPermittedException
            // e traduz para RestClientException — sem ir ao HTTP.
            assertThatThrownBy(() -> brapiClient.buscarCotacoes(List.of("HGLG11")))
                .isInstanceOf(RestClientException.class);

            WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/api/quote/HGLG11")));
        }
    }

    @Nested
    @DisplayName("Caminho feliz (sem falha)")
    class HappyPath {

        @Test
        @DisplayName("BrapiClient agrega resultados de múltiplos tickers em uma única resposta")
        void testBrapiClientAgrega() {
            WIREMOCK.stubFor(get(urlPathEqualTo("/api/quote/HGLG11"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(OK_RESPONSE)));
            WIREMOCK.stubFor(get(urlPathEqualTo("/api/quote/UNKNOWN"))
                .willReturn(aResponse().withStatus(404)));

            var resposta = brapiClient.buscarCotacoes(List.of("HGLG11", "UNKNOWN"));

            assertThat(resposta.results()).hasSize(1);
            assertThat(resposta.results().get(0).symbol()).isEqualTo("HGLG11");
        }
    }
}
