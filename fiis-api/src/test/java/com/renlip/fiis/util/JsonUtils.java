package com.renlip.fiis.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utilitários para manipulação de arquivos JSON em testes.
 *
 * <p>Usado principalmente para carregar os arquivos {@code actual.json}
 * (entrada dos cenários) e {@code expected.json} (saída esperada).</p>
 */
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    private JsonUtils() {
        throw new IllegalStateException("Classe utilitária — não deve ser instanciada");
    }

    /**
     * Lê o conteúdo bruto de um arquivo JSON do classpath como string.
     *
     * @param classpathPath caminho relativo dentro de {@code src/test/resources/}
     *                      (ex: {@code "scenarios/fundos/success/01-listar/expected.json"})
     * @return conteúdo do arquivo como string
     * @throws IOException se o arquivo não existir ou falhar a leitura
     */
    public static String readFile(String classpathPath) throws IOException {
        try (InputStream in = new ClassPathResource(classpathPath).getInputStream()) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        }
    }

    /**
     * Converte um arquivo JSON (objeto plano) em {@link MultiValueMap} —
     * formato usado para passar parâmetros de query para o MockMvc.
     *
     * <p>Exemplo: o JSON {@code {"tipo":"TIJOLO","ativo":true}} vira
     * {@code {tipo=[TIJOLO], ativo=[true]}}.</p>
     *
     * @param classpathPath caminho do arquivo dentro de {@code src/test/resources/}
     * @return parâmetros prontos para usar em {@code .param(...)} do MockMvc
     * @throws IOException se a leitura do arquivo falhar
     */
    public static MultiValueMap<String, String> buildMapFromFile(String classpathPath) throws IOException {
        Map<String, Object> raw = OBJECT_MAPPER.readValue(
            new ClassPathResource(classpathPath).getInputStream(),
            new TypeReference<>() {}
        );

        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        raw.forEach((key, value) -> {
            if (value != null) {
                result.add(key, value.toString());
            }
        });
        return result;
    }

    /**
     * Expõe o {@link ObjectMapper} compartilhado (já configurado com suporte
     * a {@link java.time}). Útil se o teste precisar desserializar JSON
     * para objetos específicos.
     *
     * @return ObjectMapper configurado
     */
    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }
}
