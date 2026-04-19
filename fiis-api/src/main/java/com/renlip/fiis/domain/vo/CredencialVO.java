package com.renlip.fiis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Value Object de entrada para autenticação.
 *
 * @param email e-mail do usuário (login)
 * @param senha senha em texto plano (será comparada com o hash BCrypt do banco)
 */
@Schema(description = "Credenciais de login")
public record CredencialVO(

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail em formato inválido")
    @Schema(description = "E-mail cadastrado", example = "ren@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @NotBlank(message = "Senha é obrigatória")
    @Schema(description = "Senha do usuário", example = "minhaSenhaSegura123", requiredMode = Schema.RequiredMode.REQUIRED)
    String senha

) {}
