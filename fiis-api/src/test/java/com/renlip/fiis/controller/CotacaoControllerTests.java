package com.renlip.fiis.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import com.renlip.fiis.domain.dto.brapi.BrapiQuote;
import com.renlip.fiis.domain.dto.brapi.BrapiQuoteResponse;
import com.renlip.fiis.support.BrapiClient;
import com.renlip.fiis.util.JsonUtils;

/**
 * Testes de integração do {@code CotacaoController}.
 *
 * <p>Fixtures:
 * <ul>
 *   <li>{@code /fixtures/setup.sql};</li>
 *   <li>{@code /fixtures/cotacoes/fii-script.sql} — 3 fundos e 3 cotações.
 *       O VISC11 (ID 3) é propositalmente cadastrado sem cotações.</li>
 * </ul>
 * </p>
 */
@SqlGroup({
    @Sql(value = "/fixtures/setup.sql",               executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/fixtures/cotacoes/fii-script.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
})
class CotacaoControllerTests extends AbstractControllerTests {

    @MockBean
    private BrapiClient brapiClient;

    @Nested
    @DisplayName("GET /api/cotacoes")
    class Get {

        @Nested
        @DisplayName("[2xx Success]")
        class GetSuccess {

            @Test
            @DisplayName("[200 OK] Lista todas as cotações")
            void testListarTodas() {
                restTestClient.get("/api/cotacoes")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/cotacoes/success/01-listar-todas/expected.json");
            }

            @Test
            @DisplayName("[200 OK] Lista cotações filtrando por fundo")
            void testListarPorFundo() {
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("fundoId", "1");

                restTestClient.get("/api/cotacoes", params)
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/cotacoes/success/02-listar-por-fundo/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("GET /api/cotacoes/{id}")
    class GetById {

        @Nested
        @DisplayName("[2xx Success]")
        class GetByIdSuccess {

            @Test
            @DisplayName("[200 OK] Retorna a cotação pelo ID")
            void testBuscarPorId() {
                restTestClient.get("/api/cotacoes/1")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/cotacoes/success/03-buscar-por-id/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class GetByIdFailure {

            @Test
            @DisplayName("[404 Not Found] Cotação não encontrada")
            void testBuscarNaoEncontrado() {
                restTestClient.get("/api/cotacoes/999")
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/cotacoes/failure/01-buscar-nao-encontrado/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("GET /api/cotacoes/ultima/{fundoId}")
    class GetUltima {

        @Nested
        @DisplayName("[2xx Success]")
        class GetUltimaSuccess {

            @Test
            @DisplayName("[200 OK] Retorna a cotação mais recente do fundo")
            void testUltimaCotacao() {
                restTestClient.get("/api/cotacoes/ultima/1")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/cotacoes/success/04-ultima-cotacao/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class GetUltimaFailure {

            @Test
            @DisplayName("[404 Not Found] Fundo existe mas não tem cotações")
            void testUltimaCotacaoSemRegistro() {
                restTestClient.get("/api/cotacoes/ultima/3")
                    .expectStatus(HttpStatus.NOT_FOUND);
            }
        }
    }

    @Nested
    @DisplayName("POST /api/cotacoes")
    class Post {

        @Nested
        @DisplayName("[2xx Success]")
        class PostSuccess {

            @Test
            @DisplayName("[201 Created] Cria uma nova cotação")
            void testCriar() throws IOException {
                String body = JsonUtils.readFile("scenarios/cotacoes/success/05-criar/actual.json");

                restTestClient.post("/api/cotacoes", body)
                    .expectStatus(HttpStatus.CREATED)
                    .expectBody("scenarios/cotacoes/success/05-criar/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class PostFailure {

            @Test
            @DisplayName("[404 Not Found] Fundo informado não existe")
            void testCriarFundoInexistente() throws IOException {
                String body = JsonUtils.readFile("scenarios/cotacoes/failure/03-criar-fundo-inexistente/actual.json");

                restTestClient.post("/api/cotacoes", body)
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/cotacoes/failure/03-criar-fundo-inexistente/expected.json");
            }

            @Test
            @DisplayName("[409 Conflict] Já existe cotação do fundo na data")
            void testCriarDuplicada() throws IOException {
                String body = JsonUtils.readFile("scenarios/cotacoes/failure/04-criar-duplicada/actual.json");

                restTestClient.post("/api/cotacoes", body)
                    .expectStatus(HttpStatus.CONFLICT)
                    .expectBody("scenarios/cotacoes/failure/04-criar-duplicada/expected.json");
            }

            @Test
            @DisplayName("[409 Conflict] Preço mínimo maior que o máximo")
            void testCriarMinimoMaiorMaximo() throws IOException {
                String body = JsonUtils.readFile("scenarios/cotacoes/failure/05-criar-minimo-maior-maximo/actual.json");

                restTestClient.post("/api/cotacoes", body)
                    .expectStatus(HttpStatus.CONFLICT)
                    .expectBody("scenarios/cotacoes/failure/05-criar-minimo-maior-maximo/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] Campos obrigatórios ausentes")
            void testCriarCamposObrigatorios() throws IOException {
                String body = JsonUtils.readFile("scenarios/cotacoes/failure/06-criar-campos-obrigatorios/actual.json");

                restTestClient.post("/api/cotacoes", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/cotacoes/failure/06-criar-campos-obrigatorios/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] Data da cotação não pode ser futura")
            void testCriarDataFutura() throws IOException {
                String body = JsonUtils.readFile("scenarios/cotacoes/failure/07-criar-data-futura/actual.json");

                restTestClient.post("/api/cotacoes", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/cotacoes/failure/07-criar-data-futura/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/cotacoes/{id}")
    class Put {

        @Nested
        @DisplayName("[2xx Success]")
        class PutSuccess {

            @Test
            @DisplayName("[200 OK] Atualiza a cotação")
            void testAtualizar() throws IOException {
                String body = JsonUtils.readFile("scenarios/cotacoes/success/06-atualizar/actual.json");

                restTestClient.put("/api/cotacoes/{id}", body, 1L)
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/cotacoes/success/06-atualizar/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("POST /api/cotacoes/importar-brapi")
    class ImportarBrapi {

        @Nested
        @DisplayName("[2xx Success]")
        class ImportarBrapiSuccess {

            @Test
            @DisplayName("[200 OK] Importa cotações da BRAPI e marca ticker ausente")
            void testImportarComTickerAusente() {
                // A fixture tem 3 fundos ativos (HGLG11, MXRF11, VISC11).
                // BRAPI retorna apenas HGLG11 e VISC11 — MXRF11 deve vir em
                // naoEncontradosBrapi.
                BrapiQuoteResponse resposta = new BrapiQuoteResponse(List.of(
                    new BrapiQuote(
                        "HGLG11",
                        new BigDecimal("160.25"),
                        new BigDecimal("158.90"),
                        new BigDecimal("158.00"),
                        new BigDecimal("161.50"),
                        new BigDecimal("1250000.00")),
                    new BrapiQuote(
                        "VISC11",
                        new BigDecimal("11.40"),
                        new BigDecimal("11.20"),
                        new BigDecimal("11.10"),
                        new BigDecimal("11.55"),
                        new BigDecimal("800000.00"))
                ));
                when(brapiClient.buscarCotacoes(any())).thenReturn(resposta);

                restTestClient.post("/api/cotacoes/importar-brapi", "")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/cotacoes/success/09-importar-brapi/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class ImportarBrapiFailure {

            @Test
            @DisplayName("[409 Conflict] BRAPI indisponível (RestClientException)")
            void testBrapiIndisponivel() {
                when(brapiClient.buscarCotacoes(any()))
                    .thenThrow(new RestClientException("boom"));

                restTestClient.post("/api/cotacoes/importar-brapi", "")
                    .expectStatus(HttpStatus.CONFLICT)
                    .expectBody("scenarios/cotacoes/failure/09-importar-brapi-indisponivel/expected.json");
            }

            @Test
            @DisplayName("[409 Conflict] Nenhum fundo ativo na carteira")
            @Sql(value = "/fixtures/cotacoes/desativar-todos-fundos.sql",
                 executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
            void testCarteiraVazia() {
                restTestClient.post("/api/cotacoes/importar-brapi", "")
                    .expectStatus(HttpStatus.CONFLICT)
                    .expectBody("scenarios/cotacoes/failure/10-importar-brapi-carteira-vazia/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/cotacoes/{id}")
    class Delete {

        @Nested
        @DisplayName("[2xx Success]")
        class DeleteSuccess {

            @Test
            @DisplayName("[204 No Content] Remove a cotação")
            void testDeletar() {
                restTestClient.delete("/api/cotacoes/{id}", 1L)
                    .expectStatus(HttpStatus.NO_CONTENT);
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class DeleteFailure {

            @Test
            @DisplayName("[404 Not Found] Cotação não existe")
            void testDeletarNaoEncontrado() {
                restTestClient.delete("/api/cotacoes/{id}", 999L)
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/cotacoes/failure/08-deletar-nao-encontrado/expected.json");
            }
        }
    }
}
