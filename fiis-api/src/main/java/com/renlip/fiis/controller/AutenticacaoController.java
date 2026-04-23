package com.renlip.fiis.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.renlip.fiis.domain.dto.TokenResponse;
import com.renlip.fiis.domain.vo.CredencialVO;
import com.renlip.fiis.domain.vo.EsqueciSenhaVO;
import com.renlip.fiis.domain.vo.ResetSenhaVO;
import com.renlip.fiis.domain.vo.SignupVO;
import com.renlip.fiis.service.AutenticacaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints de autenticação.
 *
 * <p>A anotação {@link SecurityRequirements} vazia remove o cadeado global do Swagger
 * para o endpoint de login, deixando claro que ele não exige token prévio.</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login e geração de tokens JWT")
@SecurityRequirements
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Autentica e retorna um token JWT",
        description = "Recebe e-mail e senha. Retorna um token JWT a ser usado no header Authorization dos demais endpoints.")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody final CredencialVO credencial) {
        return ResponseEntity.ok(autenticacaoService.login(credencial));
    }

    @PostMapping(path = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria uma nova conta e retorna um token JWT",
        description = "Cadastra um novo usuário com perfil USER e devolve um token JWT já autenticado (auto-login após cadastro).")
    public ResponseEntity<TokenResponse> signup(
            @Valid @RequestBody final SignupVO signup,
            final HttpServletRequest request) {
        String ip = extrairIpCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(autenticacaoService.signup(signup, ip));
    }

    @PostMapping(path = "/forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Envia email de redefinição de senha",
        description = "Se o e-mail pertencer a uma conta ativa, um link de redefinição é enviado. "
            + "A resposta é sempre 204 No Content para não revelar quais e-mails estão cadastrados.")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody final EsqueciSenhaVO esqueci) {
        autenticacaoService.forgotPassword(esqueci);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Troca a senha a partir de um token válido",
        description = "Consome o token recebido por email e aplica a nova senha. "
            + "A política de senha forte (mínimo 8 caracteres com letra e número) é aplicada.")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody final ResetSenhaVO reset) {
        autenticacaoService.resetPassword(reset);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extrai o IP real do cliente respeitando proxy reverso. Render e Vercel
     * colocam o IP original no header {@code X-Forwarded-For} (lista separada
     * por vírgulas; o primeiro item é o cliente). Quando ausente, usa o
     * endereço remoto direto.
     */
    private String extrairIpCliente(final HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int virgula = forwardedFor.indexOf(',');
            return (virgula > 0 ? forwardedFor.substring(0, virgula) : forwardedFor).trim();
        }
        return request.getRemoteAddr();
    }
}
