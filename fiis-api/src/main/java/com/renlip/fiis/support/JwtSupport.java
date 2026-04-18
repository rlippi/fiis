package com.renlip.fiis.support;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.renlip.fiis.config.security.JwtUserDetails;
import com.renlip.fiis.domain.entity.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Componente responsável por gerar e validar tokens JWT.
 *
 * <p>Usa {@code HS256} (HMAC-SHA256) com segredo compartilhado — simples, suficiente
 * para uma aplicação stateless com um único emissor. Se no futuro tiver múltiplos
 * serviços validando o mesmo token, vale migrar para {@code RS256} (chave assimétrica).</p>
 */
@Slf4j
@Component
public class JwtSupport {

    private static final String CLAIM_NOME = "nome";

    private static final String CLAIM_PERFIL = "perfil";

    private final SecretKey secretKey;

    private final long ttlMillis;

    private final String issuer;

    public JwtSupport(
            @Value("${fiis.jwt.secret}") final String secret,
            @Value("${fiis.jwt.ttl-millis}") final long ttlMillis,
            @Value("${fiis.jwt.issuer}") final String issuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMillis = ttlMillis;
        this.issuer = issuer;
    }

    /**
     * Gera um token JWT assinado para o usuário autenticado.
     *
     * @param userDetails usuário autenticado (adapter {@link JwtUserDetails})
     * @return token compacto (JWS)
     */
    public String gerarToken(final UserDetails userDetails) {
        Usuario usuario = ((JwtUserDetails) userDetails).getUsuario();
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + ttlMillis);

        return Jwts.builder()
            .subject(usuario.getEmail())
            .issuer(issuer)
            .issuedAt(agora)
            .expiration(expiracao)
            .claim(CLAIM_NOME, usuario.getNome())
            .claim(CLAIM_PERFIL, usuario.getPerfil().name())
            .signWith(secretKey)
            .compact();
    }

    /**
     * Extrai o e-mail (subject) de um token previamente validado.
     */
    public String extrairEmail(final String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Verifica se o token tem assinatura válida e não expirou.
     *
     * @param token JWT recebido no header Authorization
     * @return {@code true} se válido; {@code false} caso contrário
     */
    public boolean validarToken(final String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
