package com.renlip.fiis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Value Object dos endpoints {@code POST /api/auth/refresh} e
 * {@code POST /api/auth/logout}.
 *
 * @param refreshToken valor opaco recebido do servidor no login/signup. Em
 *                     {@code /refresh} é trocado por um novo par access+refresh;
 *                     em {@code /logout} é apenas revogado.
 */
@Schema(description = "Refresh token a ser rotacionado ou revogado")
public record RefreshTokenVO(

    @NotBlank(message = "Refresh token é obrigatório")
    @Size(max = 256, message = "Refresh token excede o tamanho máximo aceito")
    @Schema(description = "Refresh token opaco", example = "QmJ4S2tCdGV4dF9hbGVhdG9yaW8...",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String refreshToken

) {}
