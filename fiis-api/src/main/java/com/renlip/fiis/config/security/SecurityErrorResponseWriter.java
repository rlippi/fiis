package com.renlip.fiis.config.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renlip.fiis.domain.enumeration.MensagemEnum;
import com.renlip.fiis.exception.ErroResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Escreve um {@link ErroResponse} JSON diretamente na {@link HttpServletResponse}.
 *
 * <p>Usado pelos handlers de segurança ({@link JwtAuthenticationEntryPoint} e
 * {@link JwtAccessDeniedHandler}) porque eles são acionados pela cadeia de filtros
 * antes de chegarem ao {@code @RestControllerAdvice} — ou seja, precisam produzir
 * o JSON manualmente.</p>
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private final MessageSource messageSource;

    private final ObjectMapper objectMapper;

    public void escrever(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final HttpStatus status,
            final String erro,
            final MensagemEnum mensagem) throws IOException {

        ErroResponse body = ErroResponse.of(
            status.value(),
            erro,
            mensagem.getCodigo(),
            messageSource.getMessage(mensagem.getTexto(), null, PT_BR),
            request.getRequestURI()
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
