package com.renlip.fiis.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renlip.fiis.domain.entity.RefreshToken;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.Perfil;
import com.renlip.fiis.repository.RefreshTokenRepository;
import com.renlip.fiis.repository.UsuarioRepository;
import com.renlip.fiis.support.RateLimitSupport;

/**
 * Testes de integração do fluxo de Refresh Token.
 *
 * <p>Cobre:
 * <ul>
 *   <li>{@code /login} e {@code /signup} retornam {@code refreshToken} no payload;</li>
 *   <li>{@code /refresh}: rotação válida, token inexistente, expirado, revogado,
 *       e <i>reuse detection</i> (token já usado dispara revogação em massa);</li>
 *   <li>{@code /logout}: revoga o refresh sem disparar reuse detection;
 *       idempotente em token inexistente.</li>
 * </ul>
 */
@WithAnonymousUser
@DisplayName("RefreshTokenController")
@Sql(value = "/fixtures/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class RefreshTokenControllerTests extends AbstractControllerTests {

    private static final String EMAIL = "refresh@fiis.com";
    private static final String SENHA = "senha123";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RateLimitSupport rateLimit;

    private Long usuarioId;

    @BeforeEach
    void prepararUsuario() {
        rateLimit.limpar();
        Usuario usuario = usuarioRepository.save(Usuario.builder()
            .email(EMAIL)
            .senha(passwordEncoder.encode(SENHA))
            .nome("Usuário Refresh")
            .perfil(Perfil.USER)
            .ativo(true)
            .build());
        this.usuarioId = usuario.getId();
    }

    private JsonNode efetuarLogin() throws Exception {
        String body = """
            {"email":"%s","senha":"%s"}
            """.formatted(EMAIL, SENHA);
        String json = new String(
            restTestClient.post("/api/auth/login", body)
                .expectStatus(HttpStatus.OK)
                .getResult()
                .getResponse()
                .getContentAsByteArray(),
            StandardCharsets.UTF_8);
        return objectMapper.readTree(json);
    }

    @Nested
    @DisplayName("Login e signup retornam refreshToken")
    class LoginSignup {

        @Test
        @DisplayName("[200 OK] /login devolve token + refreshToken")
        void testLoginRetornaRefresh() throws Exception {
            JsonNode resp = efetuarLogin();

            assertThat(resp.get("token").asText()).isNotBlank();
            assertThat(resp.get("refreshToken").asText()).isNotBlank();
            assertThat(resp.get("token").asText())
                .isNotEqualTo(resp.get("refreshToken").asText());
        }

        @Test
        @DisplayName("[201 Created] /signup devolve token + refreshToken")
        void testSignupRetornaRefresh() throws Exception {
            String body = """
                {"nome":"Novo User","email":"novo@fiis.com","senha":"senha123ABC"}
                """;
            String json = new String(
                restTestClient.post("/api/auth/signup", body)
                    .expectStatus(HttpStatus.CREATED)
                    .getResult()
                    .getResponse()
                    .getContentAsByteArray(),
                StandardCharsets.UTF_8);
            JsonNode resp = objectMapper.readTree(json);

            assertThat(resp.get("token").asText()).isNotBlank();
            assertThat(resp.get("refreshToken").asText()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("[200 OK] rotaciona refresh válido e devolve novo par")
        void testRefreshRotaciona() throws Exception {
            JsonNode loginResp = efetuarLogin();
            String refreshAntigo = loginResp.get("refreshToken").asText();
            String accessAntigo = loginResp.get("token").asText();

            String body = """
                {"refreshToken":"%s"}
                """.formatted(refreshAntigo);
            String json = new String(
                restTestClient.post("/api/auth/refresh", body)
                    .expectStatus(HttpStatus.OK)
                    .getResult()
                    .getResponse()
                    .getContentAsByteArray(),
                StandardCharsets.UTF_8);
            JsonNode resp = objectMapper.readTree(json);

            String accessNovo = resp.get("token").asText();
            String refreshNovo = resp.get("refreshToken").asText();
            // Não comparamos accessNovo != accessAntigo porque o JWT usa
            // timestamps em segundos: se rotação ocorre no mesmo segundo do
            // login, o JWT resultante é byte-for-byte idêntico (não é bug).
            // O importante é o refresh: SEMPRE rotacionado (32 bytes random).
            assertThat(accessNovo).isNotBlank();
            assertThat(refreshNovo).isNotBlank().isNotEqualTo(refreshAntigo);
            // Variável usada apenas pelo comentário acima — silencia warning.
            assertThat(accessAntigo).isNotBlank();

            // Antigo deve ter ficado com used_at preenchido + replaced_by_id apontando pro novo.
            List<RefreshToken> doUsuario = refreshTokenRepository.findAll().stream()
                .filter(rt -> rt.getUsuario().getId().equals(usuarioId))
                .toList();
            assertThat(doUsuario).hasSize(2);
            assertThat(doUsuario).anySatisfy(rt -> {
                assertThat(rt.getUsedAt()).isNotNull();
                assertThat(rt.getReplacedBy()).isNotNull();
            });
        }

        @Test
        @DisplayName("[409 Conflict] refresh inexistente devolve FII0024")
        void testRefreshInexistente() {
            String body = """
                {"refreshToken":"token-que-nao-existe"}
                """;
            restTestClient.post("/api/auth/refresh", body)
                .expectStatus(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("[409 Conflict] refresh expirado é rejeitado")
        void testRefreshExpirado() throws Exception {
            JsonNode loginResp = efetuarLogin();
            String refresh = loginResp.get("refreshToken").asText();

            // Força expiração no banco mexendo direto na entity.
            RefreshToken rt = refreshTokenRepository.findAll().get(0);
            rt.setExpiresAt(LocalDateTime.now().minusMinutes(1));
            refreshTokenRepository.save(rt);

            String body = """
                {"refreshToken":"%s"}
                """.formatted(refresh);
            restTestClient.post("/api/auth/refresh", body)
                .expectStatus(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("[409 Conflict] reuse detection — usar token já consumido revoga todos do usuário")
        void testReuseDetection() throws Exception {
            JsonNode loginResp = efetuarLogin();
            String refresh1 = loginResp.get("refreshToken").asText();

            // 1ª rotação: válida, gera refresh2
            String body1 = """
                {"refreshToken":"%s"}
                """.formatted(refresh1);
            restTestClient.post("/api/auth/refresh", body1)
                .expectStatus(HttpStatus.OK);

            // 2ª tentativa com refresh1 (já usado) → reuse detection
            restTestClient.post("/api/auth/refresh", body1)
                .expectStatus(HttpStatus.CONFLICT);

            // Todos os refreshes do usuário devem ter revoked_at preenchido
            // (incluindo o refresh2 que era válido até o reuse detection disparar).
            List<RefreshToken> tokens = refreshTokenRepository.findAll().stream()
                .filter(rt -> rt.getUsuario().getId().equals(usuarioId))
                .filter(rt -> rt.getUsedAt() == null) // só os "ativos" antes do reuse
                .toList();
            assertThat(tokens).allSatisfy(rt ->
                assertThat(rt.getRevokedAt()).as("refresh ativo deveria ser revogado pelo reuse detection")
                    .isNotNull());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class Logout {

        @Test
        @DisplayName("[204 No Content] revoga o refresh e impede /refresh subsequente")
        void testLogoutRevogaRefresh() throws Exception {
            JsonNode loginResp = efetuarLogin();
            String refresh = loginResp.get("refreshToken").asText();

            String body = """
                {"refreshToken":"%s"}
                """.formatted(refresh);
            restTestClient.post("/api/auth/logout", body)
                .expectStatus(HttpStatus.NO_CONTENT);

            // Refresh com o mesmo token agora falha (revoked_at preenchido).
            restTestClient.post("/api/auth/refresh", body)
                .expectStatus(HttpStatus.CONFLICT);

            // Importante: revoked_at é DISTINTO de used_at — não deve disparar
            // reuse detection. Verificação: o token está revoked, não used.
            RefreshToken rt = refreshTokenRepository.findAll().stream()
                .filter(t -> t.getUsuario().getId().equals(usuarioId))
                .findFirst()
                .orElseThrow();
            assertThat(rt.getRevokedAt()).isNotNull();
            assertThat(rt.getUsedAt()).as("logout não deve marcar used_at").isNull();
        }

        @Test
        @DisplayName("[204 No Content] idempotente quando token não existe")
        void testLogoutIdempotente() {
            String body = """
                {"refreshToken":"token-que-nao-existe"}
                """;
            restTestClient.post("/api/auth/logout", body)
                .expectStatus(HttpStatus.NO_CONTENT);
        }
    }
}
