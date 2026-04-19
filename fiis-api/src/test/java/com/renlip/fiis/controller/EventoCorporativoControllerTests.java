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
 * Testes de integração do {@code EventoCorporativoController}.
 *
 * <p>Fixtures:
 * <ul>
 *   <li>{@code /fixtures/setup.sql};</li>
 *   <li>{@code /fixtures/eventos-corporativos/fii-script.sql} — 2 fundos e 2 eventos.</li>
 * </ul>
 * </p>
 */
@SqlGroup({
    @Sql(value = "/fixtures/setup.sql",                          executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/fixtures/eventos-corporativos/fii-script.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
})
class EventoCorporativoControllerTests extends AbstractControllerTests {

    @Nested
    @DisplayName("GET /api/eventos-corporativos")
    class Get {

        @Nested
        @DisplayName("[2xx Success]")
        class GetSuccess {

            @Test
            @DisplayName("[200 OK] Lista todos os eventos")
            void testListarTodos() {
                restTestClient.get("/api/eventos-corporativos")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/eventos-corporativos/success/01-listar-todos/expected.json");
            }

            @Test
            @DisplayName("[200 OK] Lista eventos filtrando por fundo")
            void testListarPorFundo() {
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("fundoId", "1");

                restTestClient.get("/api/eventos-corporativos", params)
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/eventos-corporativos/success/02-listar-por-fundo/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("GET /api/eventos-corporativos/{id}")
    class GetById {

        @Nested
        @DisplayName("[2xx Success]")
        class GetByIdSuccess {

            @Test
            @DisplayName("[200 OK] Retorna o evento pelo ID")
            void testBuscarPorId() {
                restTestClient.get("/api/eventos-corporativos/1")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/eventos-corporativos/success/03-buscar-por-id/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class GetByIdFailure {

            @Test
            @DisplayName("[404 Not Found] Evento não encontrado")
            void testBuscarNaoEncontrado() {
                restTestClient.get("/api/eventos-corporativos/999")
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/eventos-corporativos/failure/01-buscar-nao-encontrado/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("POST /api/eventos-corporativos")
    class Post {

        @Nested
        @DisplayName("[2xx Success]")
        class PostSuccess {

            @Test
            @DisplayName("[201 Created] Cria um novo evento")
            void testCriar() throws IOException {
                String body = JsonUtils.readFile("scenarios/eventos-corporativos/success/04-criar/actual.json");

                restTestClient.post("/api/eventos-corporativos", body)
                    .expectStatus(HttpStatus.CREATED)
                    .expectBody("scenarios/eventos-corporativos/success/04-criar/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class PostFailure {

            @Test
            @DisplayName("[404 Not Found] Fundo informado não existe")
            void testCriarFundoInexistente() throws IOException {
                String body = JsonUtils.readFile("scenarios/eventos-corporativos/failure/02-criar-fundo-inexistente/actual.json");

                restTestClient.post("/api/eventos-corporativos", body)
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/eventos-corporativos/failure/02-criar-fundo-inexistente/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] Campos obrigatórios ausentes")
            void testCriarCamposObrigatorios() throws IOException {
                String body = JsonUtils.readFile("scenarios/eventos-corporativos/failure/03-criar-campos-obrigatorios/actual.json");

                restTestClient.post("/api/eventos-corporativos", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/eventos-corporativos/failure/03-criar-campos-obrigatorios/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] Fator igual a zero")
            void testCriarFatorZero() throws IOException {
                String body = JsonUtils.readFile("scenarios/eventos-corporativos/failure/04-criar-fator-zero/actual.json");

                restTestClient.post("/api/eventos-corporativos", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/eventos-corporativos/failure/04-criar-fator-zero/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/eventos-corporativos/{id}")
    class Put {

        @Nested
        @DisplayName("[2xx Success]")
        class PutSuccess {

            @Test
            @DisplayName("[200 OK] Atualiza o evento")
            void testAtualizar() throws IOException {
                String body = JsonUtils.readFile("scenarios/eventos-corporativos/success/05-atualizar/actual.json");

                restTestClient.put("/api/eventos-corporativos/{id}", body, 1L)
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/eventos-corporativos/success/05-atualizar/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/eventos-corporativos/{id}")
    class Delete {

        @Nested
        @DisplayName("[2xx Success]")
        class DeleteSuccess {

            @Test
            @DisplayName("[204 No Content] Remove o evento")
            void testDeletar() {
                restTestClient.delete("/api/eventos-corporativos/{id}", 1L)
                    .expectStatus(HttpStatus.NO_CONTENT);
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class DeleteFailure {

            @Test
            @DisplayName("[404 Not Found] Evento não existe")
            void testDeletarNaoEncontrado() {
                restTestClient.delete("/api/eventos-corporativos/{id}", 999L)
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/eventos-corporativos/failure/05-deletar-nao-encontrado/expected.json");
            }
        }
    }
}
