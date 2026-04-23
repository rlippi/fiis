package com.renlip.fiis.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.renlip.fiis.FiisApiApplication;
import com.renlip.fiis.domain.entity.ResetToken;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.Perfil;
import com.renlip.fiis.repository.ResetTokenRepository;
import com.renlip.fiis.repository.UsuarioRepository;
import com.renlip.fiis.support.RateLimitSupport;
import com.renlip.fiis.util.RestTestClient;

/**
 * Testes de integração do fluxo "Esqueci minha senha".
 *
 * <p>Usa {@link GreenMail} como servidor SMTP em memória (porta 3025) para
 * capturar os emails enviados pelo {@code EmailService} sem depender de SMTP
 * real. O {@link DynamicPropertySource} redireciona {@code spring.mail.*} para
 * o GreenMail e ativa {@code fiis.mail.enabled} — sem essa ativação o service
 * cairia no modo log-only e nenhum email chegaria ao servidor de teste.</p>
 */
@ActiveProfiles("test")
@SpringBootTest(classes = FiisApiApplication.class)
@AutoConfigureMockMvc
@WithAnonymousUser
@Sql(value = "/fixtures/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("AutenticacaoController — esqueci minha senha")
class EsqueciSenhaControllerTests {

    private static final GreenMail GREEN_MAIL = new GreenMail(ServerSetupTest.SMTP);

    private static final String EMAIL_ATIVO = "test@fiis.com";
    private static final String SENHA_ATUAL = "senhaAtual123";
    private static final String SENHA_NOVA = "senhaNova456";

    @DynamicPropertySource
    static void mailProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> ServerSetupTest.SMTP.getPort());
        registry.add("spring.mail.username", () -> "test");
        registry.add("spring.mail.password", () -> "test");
        registry.add("spring.mail.properties.mail.smtp.auth", () -> false);
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> false);
        registry.add("spring.mail.properties.mail.smtp.starttls.required", () -> false);
        registry.add("fiis.mail.enabled", () -> true);
    }

    @BeforeAll
    static void startGreenMail() {
        GREEN_MAIL.start();
        // GreenMail aceita autenticação se houver um usuário cadastrado com a
        // mesma credencial do cliente SMTP. Sem isso o envio falha com
        // "Authentication failed" porque o spring-boot-starter-mail por padrão
        // manda AUTH no protocolo.
        GREEN_MAIL.setUser("test", "test");
    }

    @AfterAll
    static void stopGreenMail() {
        GREEN_MAIL.stop();
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ResetTokenRepository resetTokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RateLimitSupport rateLimit;

    private RestTestClient restTestClient;
    private Usuario usuarioAtivo;

    @BeforeEach
    void prepararCenario() throws Exception {
        this.restTestClient = new RestTestClient(mockMvc);
        rateLimit.limpar();
        GREEN_MAIL.purgeEmailFromAllMailboxes();

        usuarioRepository.deleteAll();
        usuarioAtivo = usuarioRepository.save(Usuario.builder()
            .email(EMAIL_ATIVO)
            .senha(passwordEncoder.encode(SENHA_ATUAL))
            .nome("Usuário de Teste")
            .perfil(Perfil.USER)
            .ativo(true)
            .build());
        usuarioRepository.save(Usuario.builder()
            .email("inativo@fiis.com")
            .senha(passwordEncoder.encode(SENHA_ATUAL))
            .nome("Usuário Inativo")
            .perfil(Perfil.USER)
            .ativo(false)
            .build());
    }

    @Test
    @DisplayName("[204] POST /forgot-password envia email e cria reset_token para e-mail ativo")
    void forgotPasswordSucesso() throws Exception {
        restTestClient.post("/api/auth/forgot-password", """
            {"email": "%s"}
            """.formatted(EMAIL_ATIVO))
            .expectStatus(HttpStatus.NO_CONTENT);

        assertThat(GREEN_MAIL.getReceivedMessages()).hasSize(1);
        MimeMessage email = GREEN_MAIL.getReceivedMessages()[0];
        assertThat(email.getAllRecipients()[0].toString()).isEqualTo(EMAIL_ATIVO);
        assertThat(email.getSubject()).contains("Redefinição de senha");
        String corpo = email.getContent().toString();
        assertThat(corpo).contains("/reset-senha?token=");
        assertThat(corpo).contains("Usuário de Teste");

        assertThat(resetTokenRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("[204] POST /forgot-password é silencioso quando o e-mail não existe")
    void forgotPasswordEmailInexistente() {
        restTestClient.post("/api/auth/forgot-password", """
            {"email": "naoexiste@fiis.com"}
            """)
            .expectStatus(HttpStatus.NO_CONTENT);

        assertThat(GREEN_MAIL.getReceivedMessages()).isEmpty();
        assertThat(resetTokenRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("[204] POST /forgot-password é silencioso quando o usuário está inativo")
    void forgotPasswordUsuarioInativo() {
        restTestClient.post("/api/auth/forgot-password", """
            {"email": "inativo@fiis.com"}
            """)
            .expectStatus(HttpStatus.NO_CONTENT);

        assertThat(GREEN_MAIL.getReceivedMessages()).isEmpty();
        assertThat(resetTokenRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("[204] POST /reset-password troca a senha quando o token é válido")
    void resetPasswordSucesso() {
        String token = criarTokenValido(usuarioAtivo);

        restTestClient.post("/api/auth/reset-password", """
            {"token": "%s", "novaSenha": "%s"}
            """.formatted(token, SENHA_NOVA))
            .expectStatus(HttpStatus.NO_CONTENT);

        Usuario recarregado = usuarioRepository.findById(usuarioAtivo.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(SENHA_NOVA, recarregado.getSenha())).isTrue();
        assertThat(passwordEncoder.matches(SENHA_ATUAL, recarregado.getSenha())).isFalse();

        ResetToken consumido = resetTokenRepository.findByToken(token).orElseThrow();
        assertThat(consumido.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("[409] POST /reset-password rejeita token inexistente")
    void resetPasswordTokenInexistente() {
        restTestClient.post("/api/auth/reset-password", """
            {"token": "nao-existe", "novaSenha": "%s"}
            """.formatted(SENHA_NOVA))
            .expectStatus(HttpStatus.CONFLICT)
            .expectBody("scenarios/auth/reset/failure/01-token-inexistente/expected.json");
    }

    @Test
    @DisplayName("[409] POST /reset-password rejeita token já consumido")
    void resetPasswordTokenJaUsado() {
        String token = criarTokenValido(usuarioAtivo);
        restTestClient.post("/api/auth/reset-password", """
            {"token": "%s", "novaSenha": "%s"}
            """.formatted(token, SENHA_NOVA))
            .expectStatus(HttpStatus.NO_CONTENT);

        restTestClient.post("/api/auth/reset-password", """
            {"token": "%s", "novaSenha": "outraSenha789"}
            """.formatted(token))
            .expectStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("[409] POST /reset-password rejeita token expirado")
    void resetPasswordTokenExpirado() {
        ResetToken expirado = resetTokenRepository.save(ResetToken.builder()
            .usuario(usuarioAtivo)
            .token("token-expirado")
            .expiresAt(LocalDateTime.now().minusMinutes(1))
            .build());

        restTestClient.post("/api/auth/reset-password", """
            {"token": "%s", "novaSenha": "%s"}
            """.formatted(expirado.getToken(), SENHA_NOVA))
            .expectStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("[400] POST /reset-password rejeita senha que não atende à política")
    void resetPasswordSenhaFraca() {
        String token = criarTokenValido(usuarioAtivo);

        restTestClient.post("/api/auth/reset-password", """
            {"token": "%s", "novaSenha": "senhafraca"}
            """.formatted(token))
            .expectStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("ao emitir novo token, os anteriores do mesmo usuário são invalidados")
    void forgotPasswordInvalidaTokensAnteriores() {
        String tokenAntigo = criarTokenValido(usuarioAtivo);

        restTestClient.post("/api/auth/forgot-password", """
            {"email": "%s"}
            """.formatted(EMAIL_ATIVO))
            .expectStatus(HttpStatus.NO_CONTENT);

        ResetToken antigo = resetTokenRepository.findByToken(tokenAntigo).orElseThrow();
        assertThat(antigo.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("[429] POST /forgot-password dispara rate limit após 3 solicitações para o mesmo e-mail")
    void forgotPasswordRateLimit() {
        String body = """
            {"email": "%s"}
            """.formatted(EMAIL_ATIVO);

        for (int i = 0; i < 3; i++) {
            restTestClient.post("/api/auth/forgot-password", body)
                .expectStatus(HttpStatus.NO_CONTENT);
        }
        restTestClient.post("/api/auth/forgot-password", body)
            .expectStatus(HttpStatus.TOO_MANY_REQUESTS)
            .expectBody("scenarios/auth/reset/failure/02-rate-limit-forgot/expected.json");
    }

    @Test
    @DisplayName("após reset, login com senha antiga falha e com senha nova funciona")
    void resetPasswordAtualizaCredencialDeLogin() {
        String token = criarTokenValido(usuarioAtivo);
        restTestClient.post("/api/auth/reset-password", """
            {"token": "%s", "novaSenha": "%s"}
            """.formatted(token, SENHA_NOVA))
            .expectStatus(HttpStatus.NO_CONTENT);

        restTestClient.post("/api/auth/login", """
            {"email": "%s", "senha": "%s"}
            """.formatted(EMAIL_ATIVO, SENHA_ATUAL))
            .expectStatus(HttpStatus.UNAUTHORIZED);

        restTestClient.post("/api/auth/login", """
            {"email": "%s", "senha": "%s"}
            """.formatted(EMAIL_ATIVO, SENHA_NOVA))
            .expectStatus(HttpStatus.OK);
    }

    private String criarTokenValido(final Usuario usuario) {
        return resetTokenRepository.save(ResetToken.builder()
            .usuario(usuario)
            .token("token-" + System.nanoTime())
            .expiresAt(LocalDateTime.now().plusMinutes(30))
            .build()).getToken();
    }

    @SuppressWarnings("unused")
    private void inspectMessage(final MimeMessage mensagem) throws Exception {
        // Helper deixado para debug se algum teste precisar inspecionar cabeçalhos além do corpo.
        for (Message.RecipientType tipo : new Message.RecipientType[] {
                Message.RecipientType.TO, Message.RecipientType.CC, Message.RecipientType.BCC }) {
            if (mensagem.getRecipients(tipo) != null) {
                System.out.println(tipo + ": " + java.util.Arrays.toString(mensagem.getRecipients(tipo)));
            }
        }
    }
}
