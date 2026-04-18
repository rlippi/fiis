package com.renlip.fiis.exception;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.renlip.fiis.domain.enumeration.MensagemEnum;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Tratador global de exceções da API.
 *
 * <p>Intercepta exceções lançadas por qualquer Controller e converte em
 * respostas HTTP padronizadas no formato {@link ErroResponse}.</p>
 *
 * <p>Assim o cliente sempre recebe o mesmo formato de erro, independente
 * do que aconteceu.</p>
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private final MessageSource messageSource;

    /**
     * Trata {@link RecursoNaoEncontradoException} → HTTP 404.
     */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNotFound(
            RecursoNaoEncontradoException ex, HttpServletRequest request) {
        ErroResponse body = ErroResponse.of(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMensagem().getCodigo(),
            resolver(ex.getMensagem(), ex.getArgs()),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Trata {@link RegraNegocioException} → HTTP 409 (Conflict).
     */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> handleBusinessRule(
            RegraNegocioException ex, HttpServletRequest request) {
        ErroResponse body = ErroResponse.of(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMensagem().getCodigo(),
            resolver(ex.getMensagem(), ex.getArgs()),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Trata erros de validação do Bean Validation → HTTP 400 (Bad Request).
     *
     * <p>Consolida todas as mensagens de erro (um campo pode ter múltiplas)
     * no campo {@code detalhes} da resposta.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList();

        ErroResponse body = ErroResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            MensagemEnum.ERRO_VALIDACAO_CAMPOS.getCodigo(),
            resolver(MensagemEnum.ERRO_VALIDACAO_CAMPOS),
            request.getRequestURI(),
            detalhes
        );
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Trata falhas de autenticação no login (credencial inválida, usuário
     * desativado, etc.) → HTTP 401.
     *
     * <p>Usa mensagem genérica propositalmente — não revela se o e-mail existe
     * ou se a senha está errada, evitando enumeração de usuários.</p>
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponse> handleAuthenticationFailure(
            AuthenticationException ex, HttpServletRequest request) {
        ErroResponse body = ErroResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            MensagemEnum.CREDENCIAIS_INVALIDAS.getCodigo(),
            resolver(MensagemEnum.CREDENCIAIS_INVALIDAS),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Fallback para qualquer exceção não tratada → HTTP 500.
     *
     * <p>Evita que o Spring exponha detalhes internos (stack trace) para o cliente.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        ErroResponse body = ErroResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            MensagemEnum.ERRO_INESPERADO.getCodigo(),
            resolver(MensagemEnum.ERRO_INESPERADO),
            request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(body);
    }

    private String resolver(final MensagemEnum mensagem, final Object... args) {
        return messageSource.getMessage(mensagem.getTexto(), toStringArgs(args), PT_BR);
    }

    /**
     * Converte os argumentos para {@code String} antes de passar ao {@link MessageSource}.
     *
     * <p>Impede que o {@link java.text.MessageFormat} aplique formatação localizada a valores
     * numéricos (ex: converter {@code 160.00} em {@code "160,00"} no locale pt_BR) e datas.</p>
     */
    private Object[] toStringArgs(final Object[] args) {
        if (args == null) {
            return null;
        }
        Object[] convertidos = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            convertidos[i] = args[i] == null ? "" : args[i].toString();
        }
        return convertidos;
    }
}
