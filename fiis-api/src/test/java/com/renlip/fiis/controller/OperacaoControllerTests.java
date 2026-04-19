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
 * Testes de integração do {@code OperacaoController}.
 *
 * <p>Fixtures carregadas antes de cada teste:
 * <ul>
 *   <li>{@code /fixtures/setup.sql};</li>
 *   <li>{@code /fixtures/operacoes/fii-script.sql} — 2 fundos e 3 operações
 *       (posição atual em HGLG11: 12 cotas).</li>
 * </ul>
 * </p>
 */
@SqlGroup({
    @Sql(value = "/fixtures/setup.sql",                executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/fixtures/operacoes/fii-script.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
})
class OperacaoControllerTests extends AbstractControllerTests {

    // =================================================================
    // GET /api/operacoes
    // =================================================================
    @Nested
    @DisplayName("GET /api/operacoes")
    class Get {

        @Nested
        @DisplayName("[2xx Success]")
        class GetSuccess {

            @Test
            @DisplayName("[200 OK] Lista todas as operações")
            void testListarTodas() {
                restTestClient.get("/api/operacoes")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/operacoes/success/01-listar-todas/expected.json");
            }

            @Test
            @DisplayName("[200 OK] Lista operações filtrando por fundo")
            void testListarPorFundo() {
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("fundoId", "1");

                restTestClient.get("/api/operacoes", params)
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/operacoes/success/02-listar-por-fundo/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class GetFailure {

            @Test
            @DisplayName("[404 Not Found] Lista por fundo inexistente")
            void testListarFundoInexistente() {
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("fundoId", "999");

                restTestClient.get("/api/operacoes", params)
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/operacoes/failure/02-listar-fundo-inexistente/expected.json");
            }
        }
    }

    // =================================================================
    // GET /api/operacoes/{id}
    // =================================================================
    @Nested
    @DisplayName("GET /api/operacoes/{id}")
    class GetById {

        @Nested
        @DisplayName("[2xx Success]")
        class GetByIdSuccess {

            @Test
            @DisplayName("[200 OK] Retorna a operação quando o ID existe")
            void testBuscarPorId() {
                restTestClient.get("/api/operacoes/1")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/operacoes/success/03-buscar-por-id/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class GetByIdFailure {

            @Test
            @DisplayName("[404 Not Found] Quando a operação não existe")
            void testBuscarNaoEncontrado() {
                restTestClient.get("/api/operacoes/999")
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/operacoes/failure/01-buscar-nao-encontrado/expected.json");
            }
        }
    }

    // =================================================================
    // POST /api/operacoes
    // =================================================================
    @Nested
    @DisplayName("POST /api/operacoes")
    class Post {

        @Nested
        @DisplayName("[2xx Success]")
        class PostSuccess {

            @Test
            @DisplayName("[201 Created] Cria uma COMPRA válida")
            void testCriarCompra() throws IOException {
                String body = JsonUtils.readFile("scenarios/operacoes/success/04-criar-compra/actual.json");

                restTestClient.post("/api/operacoes", body)
                    .expectStatus(HttpStatus.CREATED)
                    .expectBody("scenarios/operacoes/success/04-criar-compra/expected.json");
            }

            @Test
            @DisplayName("[201 Created] Cria uma VENDA dentro da posição")
            void testCriarVenda() throws IOException {
                String body = JsonUtils.readFile("scenarios/operacoes/success/05-criar-venda/actual.json");

                restTestClient.post("/api/operacoes", body)
                    .expectStatus(HttpStatus.CREATED)
                    .expectBody("scenarios/operacoes/success/05-criar-venda/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class PostFailure {

            @Test
            @DisplayName("[404 Not Found] Quando o fundo informado não existe")
            void testCriarFundoInexistente() throws IOException {
                String body = JsonUtils.readFile("scenarios/operacoes/failure/03-criar-fundo-inexistente/actual.json");

                restTestClient.post("/api/operacoes", body)
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/operacoes/failure/03-criar-fundo-inexistente/expected.json");
            }

            @Test
            @DisplayName("[409 Conflict] Quando a VENDA excede a posição atual")
            void testCriarVendaSemCotas() throws IOException {
                String body = JsonUtils.readFile("scenarios/operacoes/failure/04-criar-venda-sem-cotas/actual.json");

                restTestClient.post("/api/operacoes", body)
                    .expectStatus(HttpStatus.CONFLICT)
                    .expectBody("scenarios/operacoes/failure/04-criar-venda-sem-cotas/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] Quando campos obrigatórios não são enviados")
            void testCriarCamposObrigatorios() throws IOException {
                String body = JsonUtils.readFile("scenarios/operacoes/failure/05-criar-campos-obrigatorios/actual.json");

                restTestClient.post("/api/operacoes", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/operacoes/failure/05-criar-campos-obrigatorios/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] Quando a data da operação é futura")
            void testCriarDataFutura() throws IOException {
                String body = JsonUtils.readFile("scenarios/operacoes/failure/06-criar-data-futura/actual.json");

                restTestClient.post("/api/operacoes", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/operacoes/failure/06-criar-data-futura/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] Quando o preço unitário é zero")
            void testCriarPrecoZero() throws IOException {
                String body = JsonUtils.readFile("scenarios/operacoes/failure/07-criar-preco-zero/actual.json");

                restTestClient.post("/api/operacoes", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/operacoes/failure/07-criar-preco-zero/expected.json");
            }
        }
    }

    // =================================================================
    // PUT /api/operacoes/{id}
    // =================================================================
    @Nested
    @DisplayName("PUT /api/operacoes/{id}")
    class Put {

        @Nested
        @DisplayName("[2xx Success]")
        class PutSuccess {

            @Test
            @DisplayName("[200 OK] Atualiza a operação")
            void testAtualizar() throws IOException {
                String body = JsonUtils.readFile("scenarios/operacoes/success/06-atualizar/actual.json");

                restTestClient.put("/api/operacoes/{id}", body, 1L)
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/operacoes/success/06-atualizar/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class PutFailure {

            @Test
            @DisplayName("[404 Not Found] Quando a operação não existe")
            void testAtualizarNaoEncontrado() throws IOException {
                String body = JsonUtils.readFile("scenarios/operacoes/failure/08-atualizar-nao-encontrado/actual.json");

                restTestClient.put("/api/operacoes/{id}", body, 999L)
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/operacoes/failure/08-atualizar-nao-encontrado/expected.json");
            }
        }
    }

    // =================================================================
    // DELETE /api/operacoes/{id}
    // =================================================================
    @Nested
    @DisplayName("DELETE /api/operacoes/{id}")
    class Delete {

        @Nested
        @DisplayName("[2xx Success]")
        class DeleteSuccess {

            @Test
            @DisplayName("[204 No Content] Remove a operação existente")
            void testDeletar() {
                restTestClient.delete("/api/operacoes/{id}", 1L)
                    .expectStatus(HttpStatus.NO_CONTENT);
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class DeleteFailure {

            @Test
            @DisplayName("[404 Not Found] Quando a operação não existe")
            void testDeletarNaoEncontrado() {
                restTestClient.delete("/api/operacoes/{id}", 999L)
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/operacoes/failure/09-deletar-nao-encontrado/expected.json");
            }
        }
    }
}
