package com.renlip.fiis.domain.vo;

import com.renlip.fiis.domain.enumeration.Segmento;
import com.renlip.fiis.domain.enumeration.TipoFundo;
import com.renlip.fiis.validator.CnpjValido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Value Object de entrada para criação ou atualização de um Fundo.
 *
 * <p>Usa <b>Bean Validation</b> (jakarta.validation) para validar os dados
 * recebidos antes de chegarem ao Service. Se uma validação falhar, o Spring
 * retorna HTTP 400 automaticamente.</p>
 *
 * <p>Implementado como <b>record</b> (Java 14+) — imutável, conciso, sem boilerplate.</p>
 *
 * @param ticker   código do fundo na B3 (ex: "HGLG11")
 * @param nome     nome oficial do fundo
 * @param cnpj     CNPJ do fundo (apenas dígitos, pode ser nulo)
 * @param tipo     tipo do fundo (Tijolo, Papel, Híbrido ou FoF)
 * @param segmento segmento de atuação (Logística, Shopping, etc.)
 * @param ativo    indica se o fundo está ativo na carteira (padrão: true)
 */
@Schema(description = "Dados para criação/atualização de um Fundo")
public record FundoRequest(

    @NotBlank(message = "Ticker é obrigatório")
    @Size(min = 4, max = 10, message = "Ticker deve ter entre 4 e 10 caracteres")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Ticker deve conter apenas letras maiúsculas e números")
    @Schema(description = "Ticker do fundo na B3", example = "HGLG11", requiredMode = Schema.RequiredMode.REQUIRED)
    String ticker,

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    @Schema(description = "Nome oficial do fundo", example = "CSHG Logística FII", requiredMode = Schema.RequiredMode.REQUIRED)
    String nome,

    @CnpjValido
    @Schema(description = "CNPJ do fundo (14 dígitos, com ou sem máscara)", example = "11728688000147")
    String cnpj,

    @NotNull(message = "Tipo é obrigatório")
    @Schema(description = "Tipo do fundo", example = "TIJOLO", requiredMode = Schema.RequiredMode.REQUIRED)
    TipoFundo tipo,

    @NotNull(message = "Segmento é obrigatório")
    @Schema(description = "Segmento de atuação", example = "LOGISTICA", requiredMode = Schema.RequiredMode.REQUIRED)
    Segmento segmento,

    @Schema(description = "Indica se o fundo está ativo na carteira (padrão: true)", example = "true")
    Boolean ativo

) {}
