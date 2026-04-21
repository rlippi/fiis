package com.renlip.fiis.domain.vo;

import com.renlip.fiis.validator.SenhaForte;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Value Object de entrada para criação de uma nova conta de usuário.
 *
 * <p>A senha passa pela política {@link SenhaForte} (mín. 8 caracteres, ao menos
 * uma letra e um número). Usuários cadastrados antes dessa política permanecem
 * com a senha que tinham — a política só vale para novos signups e resets.</p>
 *
 * @param nome  nome completo do usuário (máx. 100 chars)
 * @param email e-mail (será o login) — deve ser único no banco
 * @param senha senha em texto plano (será armazenada em BCrypt)
 */
@Schema(description = "Dados de cadastro de novo usuário")
public record SignupVO(

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Schema(description = "Nome completo", example = "João da Silva", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    String nome,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail em formato inválido")
    @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
    @Schema(description = "E-mail para login", example = "joao@example.com", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 150)
    String email,

    @NotBlank(message = "Senha é obrigatória")
    @Size(max = 50, message = "Senha deve ter no máximo 50 caracteres")
    @SenhaForte
    @Schema(description = "Senha (mínimo 8 caracteres, com pelo menos uma letra e um número)",
        example = "minhaSenha123", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8, maxLength = 50)
    String senha

) {}
