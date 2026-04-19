package com.renlip.fiis.domain.dto;

import com.renlip.fiis.domain.enumeration.Perfil;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída do endpoint de autenticação.
 *
 * @param token       token JWT para uso no header {@code Authorization: Bearer <token>}
 * @param tipo        tipo do token (sempre {@code Bearer})
 * @param nome        nome do usuário autenticado (exibição)
 * @param perfil      perfil do usuário autenticado
 * @param expiraEmMs  TTL do token em milissegundos (referência para o cliente)
 */
@Schema(description = "Resposta do login com o token JWT")
public record TokenResponse(

    @Schema(description = "Token JWT a ser usado no header Authorization", example = "eyJhbGciOiJIUzI1NiJ9...")
    String token,

    @Schema(description = "Tipo do token", example = "Bearer")
    String tipo,

    @Schema(description = "Nome do usuário autenticado", example = "Renato Lippi")
    String nome,

    @Schema(description = "Perfil do usuário autenticado", example = "ADMIN")
    Perfil perfil,

    @Schema(description = "Tempo de vida do token em milissegundos", example = "86400000")
    long expiraEmMs

) {}
