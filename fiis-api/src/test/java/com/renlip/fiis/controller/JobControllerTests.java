package com.renlip.fiis.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renlip.fiis.domain.dto.brapi.BrapiQuote;
import com.renlip.fiis.domain.dto.brapi.BrapiQuoteResponse;
import com.renlip.fiis.support.BrapiClient;

/**
 * Testes de integração do {@code JobController}.
 *
 * <p>Fixtures:
 * <ul>
 *   <li>{@code /fixtures/setup.sql} — cria test@fiis.com (ID 1, USER, ativo);</li>
 *   <li>{@code /fixtures/jobs/fii-script.sql} — adiciona admin@fiis.com (ID 2, ADMIN, ativo)
 *       e outro@fiis.com (ID 3, USER, ativo), cada um com 1 fundo ativo de ticker distinto.</li>
 * </ul>
 */
@SqlGroup({
    @Sql(value = "/fixtures/setup.sql",       executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/fixtures/jobs/fii-script.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
})
class JobControllerTests extends AbstractControllerTests {

    @MockBean
    private BrapiClient brapiClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/jobs/atualizar-cotacoes")
    class AtualizarCotacoes {

        @Nested
        @DisplayName("[4xx Security]")
        class Security {

            @Test
            @DisplayName("[403 Forbidden] USER não pode disparar jobs administrativos")
            void testUserNaoPodeDispararJob() {
                // AbstractControllerTests autentica como test@fiis.com (USER).
                restTestClient.post("/api/jobs/atualizar-cotacoes", "")
                    .expectStatus(HttpStatus.FORBIDDEN);
            }
        }

        @Nested
        @DisplayName("[2xx Success]")
        @WithUserDetails("admin@fiis.com")
        class Success {

            @Test
            @DisplayName("[200 OK] ADMIN dispara job — todos os usuários processados com sucesso")
            void testAdminDisparaHappyPath() throws IOException {
                // Mock devolve quote válida para qualquer ticker que chegar.
                when(brapiClient.buscarCotacoes(any())).thenAnswer(invocation -> {
                    List<String> tickers = invocation.getArgument(0);
                    List<BrapiQuote> quotes = tickers.stream()
                        .map(t -> new BrapiQuote(t,
                            new BigDecimal("100.00"),
                            new BigDecimal("99.50"),
                            new BigDecimal("99.00"),
                            new BigDecimal("101.00"),
                            new BigDecimal("500000.00")))
                        .toList();
                    return new BrapiQuoteResponse(quotes);
                });

                MvcResult result = restTestClient.post("/api/jobs/atualizar-cotacoes", "")
                    .expectStatus(HttpStatus.OK)
                    .getResult();

                JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
                assertThat(body.get("usuariosProcessados").asInt()).isEqualTo(3);
                assertThat(body.get("comSucesso").asInt()).isEqualTo(3);
                assertThat(body.get("comFalha").asInt()).isZero();
                assertThat(body.get("cotacoesCriadas").asInt()).isEqualTo(3);
                assertThat(body.get("cotacoesAtualizadas").asInt()).isZero();
            }

            @Test
            @DisplayName("[200 OK] ADMIN dispara job — falha isolada em 1 usuário é contabilizada")
            void testAdminDisparaComFalhaIsolada() throws IOException {
                // BRAPI devolve erro só para o usuário com ticker FAIL11 (admin@fiis.com).
                // Os outros 2 usuários são processados normalmente.
                when(brapiClient.buscarCotacoes(any())).thenAnswer(invocation -> {
                    List<String> tickers = invocation.getArgument(0);
                    if (tickers.contains("FAIL11")) {
                        throw new RestClientException("BRAPI indisponível (simulado)");
                    }
                    List<BrapiQuote> quotes = tickers.stream()
                        .map(t -> new BrapiQuote(t,
                            new BigDecimal("100.00"),
                            null, null, null, null))
                        .toList();
                    return new BrapiQuoteResponse(quotes);
                });

                MvcResult result = restTestClient.post("/api/jobs/atualizar-cotacoes", "")
                    .expectStatus(HttpStatus.OK)
                    .getResult();

                JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
                assertThat(body.get("usuariosProcessados").asInt()).isEqualTo(3);
                assertThat(body.get("comSucesso").asInt()).isEqualTo(2);
                assertThat(body.get("comFalha").asInt()).isEqualTo(1);
                assertThat(body.get("cotacoesCriadas").asInt()).isEqualTo(2);
                assertThat(body.get("cotacoesAtualizadas").asInt()).isZero();
            }
        }
    }
}
