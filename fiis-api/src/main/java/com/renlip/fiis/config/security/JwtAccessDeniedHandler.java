package com.renlip.fiis.config.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.renlip.fiis.domain.enumeration.MensagemEnum;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Resposta 403 (Forbidden) quando o usuário autenticado não tem permissão
 * suficiente para acessar a rota.
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityErrorResponseWriter responseWriter;

    @Override
    public void handle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AccessDeniedException accessDeniedException) throws IOException {

        responseWriter.escrever(request, response,
            HttpStatus.FORBIDDEN, "Forbidden",
            MensagemEnum.ACESSO_NEGADO);
    }
}
