package com.renlip.fiis.domain.vo;

import com.renlip.fiis.validator.SenhaForte;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Value Object do endpoint {@code POST /api/auth/reset-password}.
 *
 * <p>A nova senha passa pela mesma política do signup ({@link SenhaForte}).</p>
 *
 * @param token      UUID recebido por email
 * @param novaSenha  senha escolhida pelo usuário (em texto plano)
 */
@Schema(description = "Redefinição de senha a partir de um token válido")
public record ResetSenhaVO(

    @NotBlank(message = "Token é obrigatório")
    @Schema(description = "Token recebido por email", requiredMode = Schema.RequiredMode.REQUIRED)
    String token,

    @NotBlank(message = "Senha é obrigatória")
    @Size(max = 50, message = "Senha deve ter no máximo 50 caracteres")
    @SenhaForte
    @Schema(description = "Nova senha (mínimo 8 caracteres, com pelo menos uma letra e um número)",
        example = "minhaSenha123", requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 8, maxLength = 50)
    String novaSenha

) {}
