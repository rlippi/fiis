package com.renlip.fiis.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Testes de integração que validam o isolamento entre usuários.
 *
 * <p>Cenário: existem dois usuários — {@code test@fiis.com} (ID 1, dono dos fundos
 * e operações pré-carregados) e {@code outro@fiis.com} (ID 2). Todos os testes
 * aqui rodam no contexto de {@code outro@fiis.com}, que <b>não deve</b> enxergar
 * nem mexer nos dados do {@code test@fiis.com}.</p>
 *
 * <p>A anotação {@code @WithUserDetails("outro@fiis.com")} sobrescreve a da
 * classe-base ({@code test@fiis.com}), garantindo que as requisições sejam
 * autenticadas como o segundo usuário.</p>
 */
@WithUserDetails("outro@fiis.com")
@SqlGroup({
    @Sql(value = "/fixtures/setup.sql",                 executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/fixtures/isolamento/fii-script.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
})
@DisplayName("Isolamento multi-usuário")
class IsolamentoMultiUsuarioTests extends AbstractControllerTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("[200 OK] GET /api/fundos devolve lista vazia quando o usuário não tem fundos")
    void testListaVaziaParaUsuarioSemFundos() throws IOException {
        String json = new String(
            restTestClient.get("/api/fundos")
                .expectStatus(HttpStatus.OK)
                .getResult()
                .getResponse()
                .getContentAsByteArray(),
            StandardCharsets.UTF_8);

        JsonNode array = objectMapper.readTree(json);
        assertThat(array.isArray()).isTrue();
        assertThat(array.size()).isZero();
    }

    @Test
    @DisplayName("[404 Not Found] GET /api/fundos/{id} retorna 404 quando o fundo pertence a outro usuário")
    void testBuscarFundoAlheioRetorna404() {
        restTestClient.get("/api/fundos/1")
            .expectStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("[404 Not Found] PUT /api/fundos/{id} retorna 404 quando o fundo pertence a outro usuário")
    void testAtualizarFundoAlheioRetorna404() throws IOException {
        String body = """
            {
                "ticker": "HGLG11",
                "nome": "Tentativa de sequestro",
                "cnpj": "11728688000147",
                "tipo": "TIJOLO",
                "segmento": "LOGISTICA",
                "ativo": true
            }
            """;

        restTestClient.put("/api/fundos/{id}", body, 1L)
            .expectStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("[404 Not Found] DELETE /api/fundos/{id} retorna 404 quando o fundo pertence a outro usuário")
    void testDesativarFundoAlheioRetorna404() {
        restTestClient.delete("/api/fundos/{id}", 1L)
            .expectStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("[201 Created] POST /api/fundos permite cadastrar ticker já usado por outro usuário")
    void testUsuariosDiferentesPodemTerMesmoTicker() throws IOException {
        String body = """
            {
                "ticker": "HGLG11",
                "nome": "CSHG Logística FII",
                "cnpj": "11728688000147",
                "tipo": "TIJOLO",
                "segmento": "LOGISTICA",
                "ativo": true
            }
            """;

        String json = new String(
            restTestClient.post("/api/fundos", body)
                .expectStatus(HttpStatus.CREATED)
                .getResult()
                .getResponse()
                .getContentAsByteArray(),
            StandardCharsets.UTF_8);

        JsonNode response = objectMapper.readTree(json);
        assertThat(response.get("ticker").asText()).isEqualTo("HGLG11");
    }

    @Test
    @DisplayName("[200 OK] GET /api/operacoes devolve lista vazia para usuário sem operações")
    void testListaOperacoesVaziaParaUsuarioSemOperacoes() throws IOException {
        String json = new String(
            restTestClient.get("/api/operacoes")
                .expectStatus(HttpStatus.OK)
                .getResult()
                .getResponse()
                .getContentAsByteArray(),
            StandardCharsets.UTF_8);

        JsonNode array = objectMapper.readTree(json);
        assertThat(array.isArray()).isTrue();
        assertThat(array.size()).isZero();
    }

    @Test
    @DisplayName("[404 Not Found] GET /api/operacoes/{id} retorna 404 para operação alheia")
    void testBuscarOperacaoAlheiaRetorna404() {
        restTestClient.get("/api/operacoes/1")
            .expectStatus(HttpStatus.NOT_FOUND);
    }
}
