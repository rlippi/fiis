package com.renlip.fiis.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.Perfil;
import com.renlip.fiis.repository.UsuarioRepository;
import com.renlip.fiis.util.JsonUtils;

/**
 * Testes de integração do {@link AutenticacaoController}.
 *
 * <p>Não estende {@link AbstractControllerTests} porque o {@code @WithMockUser}
 * herdado interferiria no fluxo real de login. Aqui cada teste roda anônimo
 * e valida o comportamento real da autenticação JWT.</p>
 */
@WithAnonymousUser
@DisplayName("AutenticacaoController")
@Sql(value = "/fixtures/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AutenticacaoControllerTests extends AbstractControllerTests {

    private static final String SENHA_VALIDA = "senha123";
    private static final String EMAIL_ATIVO = "test@fiis.com";
    private static final String EMAIL_INATIVO = "inativo@fiis.com";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void prepararUsuariosDeTeste() {
        usuarioRepository.deleteAll();
        usuarioRepository.save(Usuario.builder()
            .email(EMAIL_ATIVO)
            .senha(passwordEncoder.encode(SENHA_VALIDA))
            .nome("Usuário de Teste")
            .perfil(Perfil.USER)
            .ativo(true)
            .build());
        usuarioRepository.save(Usuario.builder()
            .email(EMAIL_INATIVO)
            .senha(passwordEncoder.encode(SENHA_VALIDA))
            .nome("Usuário Inativo")
            .perfil(Perfil.USER)
            .ativo(false)
            .build());
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Nested
        @DisplayName("cenários de sucesso")
        class LoginSuccess {

            @Test
            @DisplayName("retorna token JWT quando credenciais são válidas")
            void testLoginComSucesso() throws Exception {
                String body = JsonUtils.readFile("scenarios/auth/success/01-login-sucesso/actual.json");

                String json = new String(
                    restTestClient.post("/api/auth/login", body)
                        .expectStatus(HttpStatus.OK)
                        .getResult()
                        .getResponse()
                        .getContentAsByteArray(),
                    StandardCharsets.UTF_8);

                JsonNode response = objectMapper.readTree(json);
                assertThat(response.get("token").asText()).isNotBlank();
                assertThat(response.get("tipo").asText()).isEqualTo("Bearer");
                assertThat(response.get("nome").asText()).isEqualTo("Usuário de Teste");
                assertThat(response.get("perfil").asText()).isEqualTo("USER");
                assertThat(response.get("expiraEmMs").asLong()).isPositive();
            }
        }

        @Nested
        @DisplayName("cenários de falha")
        class LoginFailure {

            @Test
            @DisplayName("retorna 401 quando a senha está errada")
            void testSenhaErrada() throws IOException {
                String body = JsonUtils.readFile("scenarios/auth/failure/01-senha-errada/actual.json");
                restTestClient.post("/api/auth/login", body)
                    .expectStatus(HttpStatus.UNAUTHORIZED)
                    .expectBody("scenarios/auth/failure/01-senha-errada/expected.json");
            }

            @Test
            @DisplayName("retorna 401 quando o e-mail não existe")
            void testEmailNaoExiste() throws IOException {
                String body = JsonUtils.readFile("scenarios/auth/failure/02-email-nao-existe/actual.json");
                restTestClient.post("/api/auth/login", body)
                    .expectStatus(HttpStatus.UNAUTHORIZED)
                    .expectBody("scenarios/auth/failure/02-email-nao-existe/expected.json");
            }

            @Test
            @DisplayName("retorna 400 quando o e-mail é inválido")
            void testEmailInvalido() throws IOException {
                String body = JsonUtils.readFile("scenarios/auth/failure/03-email-invalido/actual.json");
                restTestClient.post("/api/auth/login", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/auth/failure/03-email-invalido/expected.json");
            }

            @Test
            @DisplayName("retorna 400 quando e-mail e senha estão em branco")
            void testCamposObrigatorios() throws IOException {
                String body = JsonUtils.readFile("scenarios/auth/failure/04-campos-obrigatorios/actual.json");
                restTestClient.post("/api/auth/login", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/auth/failure/04-campos-obrigatorios/expected.json");
            }

            @Test
            @DisplayName("retorna 401 quando o usuário está inativo")
            void testUsuarioInativo() throws IOException {
                String body = JsonUtils.readFile("scenarios/auth/failure/05-usuario-inativo/actual.json");
                restTestClient.post("/api/auth/login", body)
                    .expectStatus(HttpStatus.UNAUTHORIZED)
                    .expectBody("scenarios/auth/failure/05-usuario-inativo/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("POST /api/auth/signup")
    class Signup {

        @Nested
        @DisplayName("cenários de sucesso")
        class SignupSuccess {

            @Test
            @DisplayName("[201 Created] cadastra usuário e retorna token JWT pronto para uso")
            void testSignupComSucesso() throws Exception {
                String body = JsonUtils.readFile("scenarios/auth/signup/success/01-signup-sucesso/actual.json");

                String json = new String(
                    restTestClient.post("/api/auth/signup", body)
                        .expectStatus(HttpStatus.CREATED)
                        .getResult()
                        .getResponse()
                        .getContentAsByteArray(),
                    StandardCharsets.UTF_8);

                JsonNode response = objectMapper.readTree(json);
                assertThat(response.get("token").asText()).isNotBlank();
                assertThat(response.get("tipo").asText()).isEqualTo("Bearer");
                assertThat(response.get("nome").asText()).isEqualTo("Novo Usuário");
                assertThat(response.get("perfil").asText()).isEqualTo("USER");
                assertThat(response.get("expiraEmMs").asLong()).isPositive();

                assertThat(usuarioRepository.findByEmail("novo@fiis.com")).isPresent();
            }
        }

        @Nested
        @DisplayName("cenários de falha")
        class SignupFailure {

            @Test
            @DisplayName("[409 Conflict] quando o e-mail já está cadastrado")
            void testSignupEmailDuplicado() throws IOException {
                String body = JsonUtils.readFile("scenarios/auth/signup/failure/01-email-ja-cadastrado/actual.json");
                restTestClient.post("/api/auth/signup", body)
                    .expectStatus(HttpStatus.CONFLICT)
                    .expectBody("scenarios/auth/signup/failure/01-email-ja-cadastrado/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] quando a senha tem menos que 8 caracteres")
            void testSignupSenhaCurta() throws IOException {
                String body = JsonUtils.readFile("scenarios/auth/signup/failure/02-senha-curta/actual.json");
                restTestClient.post("/api/auth/signup", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/auth/signup/failure/02-senha-curta/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] quando campos obrigatórios estão em branco")
            void testSignupCamposObrigatorios() throws IOException {
                String body = JsonUtils.readFile("scenarios/auth/signup/failure/03-campos-obrigatorios/actual.json");
                restTestClient.post("/api/auth/signup", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/auth/signup/failure/03-campos-obrigatorios/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] quando a senha não tem nenhuma letra")
            void testSignupSenhaSemLetra() throws IOException {
                String body = JsonUtils.readFile("scenarios/auth/signup/failure/04-senha-sem-letra/actual.json");
                restTestClient.post("/api/auth/signup", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/auth/signup/failure/04-senha-sem-letra/expected.json");
            }

            @Test
            @DisplayName("[400 Bad Request] quando a senha não tem nenhum número")
            void testSignupSenhaSemNumero() throws IOException {
                String body = JsonUtils.readFile("scenarios/auth/signup/failure/05-senha-sem-numero/actual.json");
                restTestClient.post("/api/auth/signup", body)
                    .expectStatus(HttpStatus.BAD_REQUEST)
                    .expectBody("scenarios/auth/signup/failure/05-senha-sem-numero/expected.json");
            }
        }
    }

    @Nested
    @DisplayName("Proteção de rotas")
    class ProtecaoRotas {

        @Test
        @DisplayName("retorna 401 ao acessar rota protegida sem token")
        void testRotaProtegidaSemToken() {
            restTestClient.get("/api/fundos")
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .expectBody("scenarios/auth/failure/06-rota-protegida-sem-token/expected.json");
        }
    }
}
