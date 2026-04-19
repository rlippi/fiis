package com.renlip.fiis.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;

/**
 * Cliente HTTP fluente para os testes de integração.
 *
 * <p>Encapsula o {@link MockMvc} do Spring com uma API encadeável,
 * facilitando a leitura dos testes. O estilo é:
 *
 * <pre>{@code
 * restTestClient.get("/api/fundos", queryParams)
 *     .expectStatus(HttpStatus.OK)
 *     .expectBody("scenarios/fundos/success/01-listar/expected.json");
 * }</pre>
 *
 * <p>A comparação de JSON usa {@code JSONAssert} em modo {@code LENIENT} —
 * ordem de campos não importa e campos extras são tolerados.</p>
 *
 * <p>Classe instanciada manualmente em {@code AbstractControllerTests} via
 * {@code @BeforeEach}; não é um componente Spring.</p>
 */
public class RestTestClient {

    private final MockMvc mockMvc;

    public RestTestClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    // -------------------------------------------------------------------
    // Métodos de entrada (HTTP verbs)
    // -------------------------------------------------------------------

    /**
     * Cria uma requisição GET sem parâmetros.
     */
    public RequestSpec get(String path) {
        return new RequestSpec(MockMvcRequestBuilders.get(path));
    }

    /**
     * Cria uma requisição GET com parâmetros de query.
     */
    public RequestSpec get(String path, MultiValueMap<String, String> queryParams) {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(path);
        if (queryParams != null) {
            builder.params(queryParams);
        }
        return new RequestSpec(builder);
    }

    /**
     * Cria uma requisição GET aplicando variáveis de path (ex: {@code /api/fundos/{id}}).
     */
    public RequestSpec getWithPathVars(String path, Map<String, ?> pathVariables) {
        Object[] values = pathVariables == null ? new Object[0] : pathVariables.values().toArray();
        return new RequestSpec(MockMvcRequestBuilders.get(path, values));
    }

    /**
     * Cria uma requisição POST com body JSON.
     */
    public RequestSpec post(String path, String body) {
        return new RequestSpec(MockMvcRequestBuilders.post(path)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
    }

    /**
     * Cria uma requisição PUT aplicando variáveis de path e body JSON.
     */
    public RequestSpec put(String path, String body, Object... pathVariables) {
        return new RequestSpec(MockMvcRequestBuilders.put(path, pathVariables)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
    }

    /**
     * Cria uma requisição DELETE aplicando variáveis de path.
     */
    public RequestSpec delete(String path, Object... pathVariables) {
        return new RequestSpec(MockMvcRequestBuilders.delete(path, pathVariables));
    }

    // -------------------------------------------------------------------
    // Builder da requisição — permite adicionar headers antes de executar
    // -------------------------------------------------------------------

    public class RequestSpec {

        private final MockHttpServletRequestBuilder builder;

        private RequestSpec(MockHttpServletRequestBuilder builder) {
            this.builder = builder;
        }

        /**
         * Adiciona um header HTTP à requisição (ex: Authorization, X-User-ID).
         */
        public RequestSpec header(String name, String value) {
            builder.header(name, value);
            return this;
        }

        /**
         * Executa a requisição e valida o status HTTP retornado.
         *
         * @param expected status esperado (ex: {@code HttpStatus.OK})
         * @return {@link ResponseSpec} para continuar validando o body
         */
        public ResponseSpec expectStatus(HttpStatus expected) {
            try {
                MvcResult result = mockMvc.perform(builder).andReturn();
                int actual = result.getResponse().getStatus();
                assertThat(HttpStatus.valueOf(actual))
                    .as("HTTP status inesperado (body=%s)", safeBody(result))
                    .isEqualTo(expected);
                return new ResponseSpec(result);
            } catch (Exception e) {
                throw new AssertionError("Falha ao executar requisição HTTP no teste", e);
            }
        }

        private String safeBody(MvcResult result) {
            try {
                return result.getResponse().getContentAsString();
            } catch (Exception e) {
                return "<não disponível>";
            }
        }
    }

    // -------------------------------------------------------------------
    // Asserções sobre a resposta
    // -------------------------------------------------------------------

    public static class ResponseSpec {

        private final MvcResult result;

        private ResponseSpec(MvcResult result) {
            this.result = result;
        }

        /**
         * Valida o corpo da resposta contra o conteúdo de um arquivo JSON
         * do classpath (geralmente {@code scenarios/.../expected.json}).
         *
         * <p>Usa {@code JSONAssert} em modo {@code LENIENT}: ordem de campos
         * não importa, e campos extras no retorno real são tolerados.</p>
         *
         * @param expectedJsonPath caminho relativo dentro de {@code src/test/resources/}
         * @return a própria {@link ResponseSpec} (permite encadear outras assertions)
         */
        public ResponseSpec expectBody(String expectedJsonPath) {
            try {
                String expected = JsonUtils.readFile(expectedJsonPath);
                // Lê o corpo da resposta interpretando como UTF-8.
                // MockHttpServletResponse usa ISO-8859-1 como fallback quando
                // o character encoding da resposta não está explicitamente
                // setado, o que corrompe acentos e outros caracteres.
                String actual = new String(
                    result.getResponse().getContentAsByteArray(),
                    StandardCharsets.UTF_8
                );
                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
                return this;
            } catch (JSONException | java.io.IOException e) {
                throw new AssertionError("Falha ao comparar JSON contra " + expectedJsonPath, e);
            }
        }

        /**
         * Expõe o {@link MvcResult} bruto caso o teste precise de uma
         * validação customizada (ex: inspecionar headers específicos).
         */
        public MvcResult getResult() {
            return result;
        }
    }
}
