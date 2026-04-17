package com.renlip.fiis.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

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
public class GlobalExceptionHandler {

    /**
     * Trata {@link RecursoNaoEncontradoException} → HTTP 404.
     */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNotFound(
            RecursoNaoEncontradoException ex, HttpServletRequest request) {
        ErroResponse body = ErroResponse.of(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
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
            ex.getMessage(),
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
            "Erro de validação nos campos enviados",
            request.getRequestURI(),
            detalhes
        );
        return ResponseEntity.badRequest().body(body);
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
            "Ocorreu um erro inesperado. Contate o administrador.",
            request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(body);
    }
}
