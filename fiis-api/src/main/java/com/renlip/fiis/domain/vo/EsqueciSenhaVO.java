package com.renlip.fiis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Value Object do endpoint {@code POST /api/auth/forgot-password}.
 *
 * @param email e-mail cadastrado; se existir, recebe o link de reset
 */
@Schema(description = "Solicitação de redefinição de senha")
public record EsqueciSenhaVO(

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail em formato inválido")
    @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
    @Schema(description = "E-mail cadastrado", example = "usuario@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 150)
    String email

) {}
