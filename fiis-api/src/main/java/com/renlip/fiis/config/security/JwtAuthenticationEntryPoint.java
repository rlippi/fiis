package com.renlip.fiis.config.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.renlip.fiis.domain.enumeration.MensagemEnum;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Resposta 401 (Unauthorized) quando uma rota protegida é acessada sem token válido.
 *
 * <p>Acionado pela cadeia de filtros do Spring Security antes de chegar aos controllers.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorResponseWriter responseWriter;

    @Override
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException authException) throws IOException {

        responseWriter.escrever(request, response,
            HttpStatus.UNAUTHORIZED, "Unauthorized",
            MensagemEnum.AUTENTICACAO_REQUERIDA);
    }
}
