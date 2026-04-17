package com.renlip.fiis.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

/**
 * Testes de integração do {@code PosicaoController}.
 *
 * <p>Valida os <b>cálculos de posição consolidada</b>: PM, custo, yield,
 * valor atual de mercado, variação e rentabilidade total.</p>
 *
 * <p>Fixtures:
 * <ul>
 *   <li>{@code /fixtures/setup.sql};</li>
 *   <li>{@code /fixtures/posicoes/fii-script.sql} — 2 fundos, 3 operações,
 *       1 provento e 1 cotação (posição HGLG11: 12 cotas, PM R$ 151,90).</li>
 * </ul>
 * </p>
 */
@SqlGroup({
    @Sql(value = "/fixtures/setup.sql",               executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/fixtures/posicoes/fii-script.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
})
class PosicaoControllerTests extends AbstractControllerTests {

    @Nested
    @DisplayName("GET /api/posicoes")
    class Get {

        @Nested
        @DisplayName("[2xx Success]")
        class GetSuccess {

            @Test
            @DisplayName("[200 OK] Lista posições de todos os fundos ativos")
            void testListarTodas() {
                restTestClient.get("/api/posicoes")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/posicoes/success/01-listar-todas/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("GET /api/posicoes/{fundoId}")
    class GetById {

        @Nested
        @DisplayName("[2xx Success]")
        class GetByIdSuccess {

            @Test
            @DisplayName("[200 OK] Retorna a posição do fundo com cálculos corretos")
            void testBuscarPorFundo() {
                restTestClient.get("/api/posicoes/1")
                    .expectStatus(HttpStatus.OK)
                    .expectBody("scenarios/posicoes/success/02-buscar-por-fundo/expected.json");
            }
        }

        @Nested
        @DisplayName("[4xx Failure]")
        class GetByIdFailure {

            @Test
            @DisplayName("[404 Not Found] Fundo não existe")
            void testBuscarNaoEncontrado() {
                restTestClient.get("/api/posicoes/999")
                    .expectStatus(HttpStatus.NOT_FOUND)
                    .expectBody("scenarios/posicoes/failure/01-buscar-nao-encontrado/expected.json");
            }
        }
    }
}
