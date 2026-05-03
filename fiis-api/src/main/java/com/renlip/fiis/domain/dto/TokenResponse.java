package com.renlip.fiis.domain.dto;

import com.renlip.fiis.domain.enumeration.Perfil;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída dos endpoints de autenticação ({@code /login}, {@code /signup}, {@code /refresh}).
 *
 * <p>Carrega o par <i>access token</i> (JWT curto, ~15min) + <i>refresh token</i>
 * (opaco e longo, ~7 dias) usado para renovar o access sem re-login.</p>
 *
 * @param token         JWT de acesso para o header {@code Authorization: Bearer <token>}
 * @param tipo          tipo do token (sempre {@code Bearer})
 * @param refreshToken  token opaco para chamar {@code POST /api/auth/refresh} quando o
 *                      access expirar. Persistido no servidor apenas como hash; o cliente
 *                      deve guardá-lo em local seguro (localStorage no browser, keychain
 *                      em mobile)
 * @param nome          nome do usuário autenticado (exibição)
 * @param perfil        perfil do usuário autenticado
 * @param expiraEmMs    TTL do access token em milissegundos (referência para o cliente
 *                      decidir quando rotacionar antecipadamente)
 */
@Schema(description = "Resposta da autenticação com access JWT + refresh token")
public record TokenResponse(

    @Schema(description = "JWT de acesso para o header Authorization", example = "eyJhbGciOiJIUzI1NiJ9...")
    String token,

    @Schema(description = "Tipo do token", example = "Bearer")
    String tipo,

    @Schema(description = "Refresh token opaco para renovar o access sem re-login", example = "QmJ4S2tC...")
    String refreshToken,

    @Schema(description = "Nome do usuário autenticado", example = "Renato Lippi")
    String nome,

    @Schema(description = "Perfil do usuário autenticado", example = "ADMIN")
    Perfil perfil,

    @Schema(description = "Tempo de vida do access token em milissegundos", example = "900000")
    long expiraEmMs

) {}
