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
 * Testes de integração do {@code FundoController}.
 *
 * <p>Estrutura:
 * <ul>
 *   <li>Classe externa ({@code @Nested}) por método HTTP;</li>
 *   <li>Subclasses ({@code @Nested}) por categoria (Success / Failure);</li>
 *   <li>Cada método carrega {@code actual.json} (quando há entrada) e
 *       compara o retorno com {@code expected.json}.</li>
 * </ul>
 * </p>
 *
 * <p>Fixtures carregadas antes de cada teste:
 * <ul>
 *   <li>{@code /fixtures/setup.sql} — DDL + TRUNCATE RESTART IDENTITY;</li>
 *   <li>{@code /fixtures/fundos/fii-script.sql} — 3 fundos de massa.</li>
 * </ul>
 * </p>
 */
@SqlGroup({
    @Sql(value = "/fixtures/setup.sql",             executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/fixtures/fundos/fii-script.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
})
class FundoControllerTests extends AbstractControllerTests {

    // =================================================================
    // GET /api/fundos — listagem
    // =================================================================
    @Nested
    @DisplayName("GET /api/fundos")
    class Get {

        @Nested
        @DisplayName("[2xx Success]")
        class GetSuccess {

            @Test
            @DisplayName("[200 OK] Lista todos os fundos cadastrados")
            void testListarTodos() {
                restTestClient.get("/api/fundos")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/fundos/success/01-listar-todos/expected.json");
            }

            @Test
            @DisplayName("[200 OK] Lista apenas os fundos ativos quando apenasAtivos=true")
            void testListarApenasAtivos() {
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("apenasAtivos", "true");

                restTestClient.get("/api/fundos", params)
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/fundos/success/02-listar-apenas-ativos/expected.json");
            }
        }
    }

    // =================================================================
    // GET /api/fundos/{id} — busca por ID
    // =================================================================
    @Nested
    @DisplayName("GET /api/fundos/{id}")
    class GetById {

        @Nested
        @DisplayName("[2xx Success]")
        class GetByIdSuccess {

            @Test
            @DisplayName("[200 OK] Retorna o fundo quando o ID existe")
            void testBuscarPorId() {
                restTestClient.get("/api/fundos/1")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/fundos/success/03-buscar-por-id/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class GetByIdFailure {

            @Test
            @DisplayName("[404 Not Found] Quando o fundo não existe")
            void testBuscarPorIdNaoEncontrado() {
                restTestClient.get("/api/fundos/999")
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/fundos/failure/01-buscar-nao-encontrado/expected.json");
            }
        }
    }

    // =================================================================
    // POST /api/fundos — criação
    // =================================================================
    @Nested
    @DisplayName("POST /api/fundos")
    class Post {

        @Nested
        @DisplayName("[2xx Success]")
        class PostSuccess {

            @Test
            @DisplayName("[201 Created] Cria um novo fundo com dados válidos")
            void testCriar() throws IOException {
                String body = JsonUtils.readFile("scenarios/fundos/success/04-criar-fundo/actual.json");

                restTestClient.post("/api/fundos", body)
                    .expectStatus(HttpStatus.CREATED)
                    .expectBody("scenarios/fundos/success/04-criar-fundo/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class PostFailure {

            @Test
            @DisplayName("[409 Conflict] Quando o ticker informado já existe")
            void testCriarTickerDuplicado() throws IOException {
                String body = JsonUtils.readFile("scenarios/fundos/failure/02-criar-ticker-duplicado/actual.json");

                restTestClient.post("/api/fundos", body)
                    .expectStatus(HttpStatus.CONFLICT)
                    .expectBody("scenarios/fundos/failure/02-criar-ticker-duplicado/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] Quando campos obrigatórios não são enviados")
            void testCriarCamposObrigatorios() throws IOException {
                String body = JsonUtils.readFile("scenarios/fundos/failure/03-criar-campos-obrigatorios/actual.json");

                restTestClient.post("/api/fundos", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/fundos/failure/03-criar-campos-obrigatorios/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] Quando ticker não atende ao pattern (letras maiúsculas e números)")
            void testCriarTickerInvalido() throws IOException {
                String body = JsonUtils.readFile("scenarios/fundos/failure/04-criar-ticker-invalido/actual.json");

                restTestClient.post("/api/fundos", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/fundos/failure/04-criar-ticker-invalido/expected.json");
            }
        }
    }

    // =================================================================
    // PUT /api/fundos/{id} — atualização
    // =================================================================
    @Nested
    @DisplayName("PUT /api/fundos/{id}")
    class Put {

        @Nested
        @DisplayName("[2xx Success]")
        class PutSuccess {

            @Test
            @DisplayName("[200 OK] Atualiza o fundo existente")
            void testAtualizar() throws IOException {
                String body = JsonUtils.readFile("scenarios/fundos/success/05-atualizar-fundo/actual.json");

                restTestClient.put("/api/fundos/{id}", body, 1L)
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/fundos/success/05-atualizar-fundo/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class PutFailure {

            @Test
            @DisplayName("[404 Not Found] Quando o fundo a atualizar não existe")
            void testAtualizarNaoEncontrado() throws IOException {
                String body = JsonUtils.readFile("scenarios/fundos/failure/05-atualizar-nao-encontrado/actual.json");

                restTestClient.put("/api/fundos/{id}", body, 999L)
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/fundos/failure/05-atualizar-nao-encontrado/expected.json");
            }

            @Test
            @DisplayName("[409 Conflict] Quando tenta assumir um ticker já usado por outro fundo")
            void testAtualizarTickerDuplicado() throws IOException {
                String body = JsonUtils.readFile("scenarios/fundos/failure/06-atualizar-ticker-duplicado/actual.json");

                restTestClient.put("/api/fundos/{id}", body, 2L)
                    .expectStatus(HttpStatus.CONFLICT)
                    .expectBody("scenarios/fundos/failure/06-atualizar-ticker-duplicado/expected.json");
            }
        }
    }

    // =================================================================
    // DELETE /api/fundos/{id} — desativação (soft delete)
    // =================================================================
    @Nested
    @DisplayName("DELETE /api/fundos/{id}")
    class Delete {

        @Nested
        @DisplayName("[2xx Success]")
        class DeleteSuccess {

            @Test
            @DisplayName("[204 No Content] Desativa o fundo existente")
            void testDesativar() {
                restTestClient.delete("/api/fundos/{id}", 1L)
                    .expectStatus(HttpStatus.NO_CONTENT);
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class DeleteFailure {

            @Test
            @DisplayName("[404 Not Found] Quando o fundo a desativar não existe")
            void testDesativarNaoEncontrado() {
                restTestClient.delete("/api/fundos/{id}", 999L)
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/fundos/failure/07-desativar-nao-encontrado/expected.json");
            }
        }
    }
}
