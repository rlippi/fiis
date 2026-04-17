package com.renlip.fiis.controller;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.renlip.fiis.util.JsonUtils;

/**
 * Testes de integração do {@code ProventoController}.
 *
 * <p>Fixtures:
 * <ul>
 *   <li>{@code /fixtures/setup.sql};</li>
 *   <li>{@code /fixtures/proventos/fii-script.sql} — 2 fundos e 3 proventos.</li>
 * </ul>
 * </p>
 */
@SqlGroup({
    @Sql(value = "/fixtures/setup.sql",                executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/fixtures/proventos/fii-script.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
})
class ProventoControllerTests extends AbstractControllerTests {

    @Nested
    @DisplayName("GET /api/proventos")
    class Get {

        @Nested
        @DisplayName("[2xx Success]")
        class GetSuccess {

            @Test
            @DisplayName("[200 OK] Lista todos os proventos")
            void testListarTodos() {
                restTestClient.get("/api/proventos")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/proventos/success/01-listar-todos/expected.json");
            }

            @Test
            @DisplayName("[200 OK] Lista proventos filtrando por fundo")
            void testListarPorFundo() {
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("fundoId", "1");

                restTestClient.get("/api/proventos", params)
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/proventos/success/02-listar-por-fundo/expected.json");
            }

            @Test
            @DisplayName("[200 OK] Lista proventos filtrando por período de pagamento")
            void testListarPorPeriodo() {
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("inicio", "2026-04-01");
                params.add("fim", "2026-04-30");

                restTestClient.get("/api/proventos", params)
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/proventos/success/03-listar-por-periodo/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class GetFailure {

            @Test
            @DisplayName("[409 Conflict] Período inicial posterior ao final")
            void testPeriodoInvalido() {
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("inicio", "2026-05-01");
                params.add("fim", "2026-01-01");

                restTestClient.get("/api/proventos", params)
                    .expectStatus(HttpStatus.CONFLICT)
                    .expectBody("scenarios/proventos/failure/02-periodo-invalido/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("GET /api/proventos/{id}")
    class GetById {

        @Nested
        @DisplayName("[2xx Success]")
        class GetByIdSuccess {

            @Test
            @DisplayName("[200 OK] Retorna o provento quando o ID existe")
            void testBuscarPorId() {
                restTestClient.get("/api/proventos/1")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/proventos/success/04-buscar-por-id/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class GetByIdFailure {

            @Test
            @DisplayName("[404 Not Found] Quando o provento não existe")
            void testBuscarNaoEncontrado() {
                restTestClient.get("/api/proventos/999")
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/proventos/failure/01-buscar-nao-encontrado/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("POST /api/proventos")
    class Post {

        @Nested
        @DisplayName("[2xx Success]")
        class PostSuccess {

            @Test
            @DisplayName("[201 Created] Cria um novo provento")
            void testCriar() throws IOException {
                String body = JsonUtils.readFile("scenarios/proventos/success/05-criar/actual.json");

                restTestClient.post("/api/proventos", body)
                    .expectStatus(HttpStatus.CREATED)
                    .expectBody("scenarios/proventos/success/05-criar/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class PostFailure {

            @Test
            @DisplayName("[404 Not Found] Fundo informado não existe")
            void testCriarFundoInexistente() throws IOException {
                String body = JsonUtils.readFile("scenarios/proventos/failure/03-criar-fundo-inexistente/actual.json");

                restTestClient.post("/api/proventos", body)
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/proventos/failure/03-criar-fundo-inexistente/expected.json");
            }

            @Test
            @DisplayName("[409 Conflict] Data de pagamento anterior à de referência")
            void testCriarDatasIncoerentes() throws IOException {
                String body = JsonUtils.readFile("scenarios/proventos/failure/04-criar-datas-incoerentes/actual.json");

                restTestClient.post("/api/proventos", body)
                    .expectStatus(HttpStatus.CONFLICT)
                    .expectBody("scenarios/proventos/failure/04-criar-datas-incoerentes/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] Campos obrigatórios ausentes")
            void testCriarCamposObrigatorios() throws IOException {
                String body = JsonUtils.readFile("scenarios/proventos/failure/05-criar-campos-obrigatorios/actual.json");

                restTestClient.post("/api/proventos", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/proventos/failure/05-criar-campos-obrigatorios/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] Valor por cota igual a zero")
            void testCriarValorZero() throws IOException {
                String body = JsonUtils.readFile("scenarios/proventos/failure/06-criar-valor-zero/actual.json");

                restTestClient.post("/api/proventos", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/proventos/failure/06-criar-valor-zero/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/proventos/{id}")
    class Put {

        @Nested
        @DisplayName("[2xx Success]")
        class PutSuccess {

            @Test
            @DisplayName("[200 OK] Atualiza o provento")
            void testAtualizar() throws IOException {
                String body = JsonUtils.readFile("scenarios/proventos/success/06-atualizar/actual.json");

                restTestClient.put("/api/proventos/{id}", body, 1L)
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/proventos/success/06-atualizar/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/proventos/{id}")
    class Delete {

        @Nested
        @DisplayName("[2xx Success]")
        class DeleteSuccess {

            @Test
            @DisplayName("[204 No Content] Remove o provento")
            void testDeletar() {
                restTestClient.delete("/api/proventos/{id}", 1L)
                    .expectStatus(HttpStatus.NO_CONTENT);
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class DeleteFailure {

            @Test
            @DisplayName("[404 Not Found] Provento não existe")
            void testDeletarNaoEncontrado() {
                restTestClient.delete("/api/proventos/{id}", 999L)
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/proventos/failure/07-deletar-nao-encontrado/expected.json");
            }
        }
    }
}
