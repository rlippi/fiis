package com.renlip.fiis.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.renlip.fiis.domain.dto.TokenResponse;
import com.renlip.fiis.domain.vo.CredencialVO;
import com.renlip.fiis.service.AutenticacaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
}
