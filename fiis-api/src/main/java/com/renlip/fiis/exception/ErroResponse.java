package com.renlip.fiis.exception;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO padrão de resposta de erro da API.
 *
 * <p>Retornado pelo {@link GlobalExceptionHandler} quando ocorre qualquer
 * exceção tratada. Mantém um formato consistente para o frontend.</p>
 *
 * @param timestamp momento em que o erro ocorreu
 * @param status    código HTTP (ex: 404, 409)
 * @param erro      descrição curta do tipo de erro (ex: "Not Found")
 * @param mensagem  mensagem explicando o que aconteceu
 * @param path      caminho do endpoint onde ocorreu o erro
 * @param detalhes  lista opcional de erros específicos (usado em validação)
 */
@Schema(description = "Resposta padronizada de erro")
public record ErroResponse(

    @Schema(description = "Momento do erro")
    LocalDateTime timestamp,

    @Schema(description = "Código HTTP", example = "404")
    int status,

    @Schema(description = "Tipo do erro", example = "Not Found")
    String erro,

    @Schema(description = "Mensagem explicativa", example = "Fundo com ID 42 não encontrado")
    String mensagem,

    @Schema(description = "Endpoint onde ocorreu o erro", example = "/api/fundos/42")
    String path,

    @Schema(description = "Detalhes adicionais (usado em erros de validação)")
    List<String> detalhes

) {

    /**
     * Factory simplificado para erros sem detalhes adicionais.
     */
    public static ErroResponse of(int status, String erro, String mensagem, String path) {
        return new ErroResponse(LocalDateTime.now(), status, erro, mensagem, path, null);
    }

    /**
     * Factory completo, com lista de detalhes (usado em erros de validação).
     */
    public static ErroResponse of(int status, String erro, String mensagem, String path, List<String> detalhes) {
        return new ErroResponse(LocalDateTime.now(), status, erro, mensagem, path, detalhes);
    }
}
