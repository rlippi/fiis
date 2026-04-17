package com.renlip.fiis.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

/**
 * Testes de integração do {@code RelatorioController}.
 *
 * <p>Valida os 5 relatórios agregados que alimentam o dashboard:
 * renda mensal, renda por fundo, alocação por tipo, alocação por segmento
 * e resumo consolidado da carteira.</p>
 *
 * <p>Fixtures:
 * <ul>
 *   <li>{@code /fixtures/setup.sql};</li>
 *   <li>{@code /fixtures/relatorios/fii-script.sql} — 2 fundos ativos,
 *       4 operações, 3 proventos em meses diferentes e cotações.</li>
 * </ul>
 * </p>
 */
@SqlGroup({
    @Sql(value = "/fixtures/setup.sql",                 executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/fixtures/relatorios/fii-script.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
})
class RelatorioControllerTests extends AbstractControllerTests {

    @Nested
    @DisplayName("GET /api/relatorios/renda-mensal")
    class RendaMensal {

        @Test
        @DisplayName("[200 OK] Agrega proventos por mês (mais recente primeiro)")
        void testRendaMensal() {
            restTestClient.get("/api/relatorios/renda-mensal")
                .expectStatus(HttpStatus.OK)
                .expectBody("scenarios/relatorios/success/01-renda-mensal/expected.json");
        }
    }

    @Nested
    @DisplayName("GET /api/relatorios/renda-por-fundo")
    class RendaPorFundo {

        @Test
        @DisplayName("[200 OK] Agrega proventos por fundo (maior recebedor primeiro)")
        void testRendaPorFundo() {
            restTestClient.get("/api/relatorios/renda-por-fundo")
                .expectStatus(HttpStatus.OK)
                .expectBody("scenarios/relatorios/success/02-renda-por-fundo/expected.json");
        }
    }

    @Nested
    @DisplayName("GET /api/relatorios/alocacao-por-tipo")
    class AlocacaoPorTipo {

        @Test
        @DisplayName("[200 OK] Distribui a carteira por tipo de fundo (Tijolo, Papel, etc.)")
        void testAlocacaoPorTipo() {
            restTestClient.get("/api/relatorios/alocacao-por-tipo")
                .expectStatus(HttpStatus.OK)
                .expectBody("scenarios/relatorios/success/03-alocacao-por-tipo/expected.json");
        }
    }

    @Nested
    @DisplayName("GET /api/relatorios/alocacao-por-segmento")
    class AlocacaoPorSegmento {

        @Test
        @DisplayName("[200 OK] Distribui a carteira por segmento (Logística, Shopping, etc.)")
        void testAlocacaoPorSegmento() {
            restTestClient.get("/api/relatorios/alocacao-por-segmento")
                .expectStatus(HttpStatus.OK)
                .expectBody("scenarios/relatorios/success/04-alocacao-por-segmento/expected.json");
        }
    }

    @Nested
    @DisplayName("GET /api/relatorios/resumo-carteira")
    class ResumoCarteira {

        @Test
        @DisplayName("[200 OK] Retorna totais consolidados da carteira")
        void testResumoCarteira() {
            restTestClient.get("/api/relatorios/resumo-carteira")
                .expectStatus(HttpStatus.OK)
                .expectBody("scenarios/relatorios/success/05-resumo-carteira/expected.json");
        }
    }
}
