package com.renlip.fiis.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.renlip.fiis.domain.enumeration.Segmento;
import com.renlip.fiis.domain.enumeration.TipoFundo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade que representa um Fundo de Investimento Imobiliário (FII).
 *
 * <p>Cada FII é identificado unicamente por seu {@code ticker} (código de
 * negociação na B3, ex: "HGLG11") e tem atributos como segmento de atuação,
 * tipo (tijolo/papel/híbrido) e CNPJ.</p>
 *
 * <p><b>Mapeamento no banco:</b> tabela {@code fundo}.</p>
 *
 * @see TipoFundo
 * @see Segmento
 */
@Entity
@Table(name = "fundo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Schema(description = "Fundo de Investimento Imobiliário (FII)")
public class Fundo {

    /**
     * Identificador único gerado automaticamente pelo banco.
     *
     * <p>Usa {@link GenerationType#IDENTITY} que delega a geração ao
     * PostgreSQL (coluna SERIAL/BIGSERIAL).</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do fundo", example = "1")
    private Long id;

    /**
     * Código de negociação do fundo na B3 (ex: "HGLG11", "MXRF11").
     * Único no banco — não pode haver dois fundos com o mesmo ticker.
     */
    @Column(nullable = false, unique = true, length = 10)
    @Schema(description = "Ticker do fundo na B3", example = "HGLG11", maxLength = 10)
    private String ticker;

    /**
     * Nome completo/oficial do fundo.
     */
    @Column(nullable = false, length = 150)
    @Schema(description = "Nome oficial do fundo", example = "CSHG Logística FII", maxLength = 150)
    private String nome;

    /**
     * CNPJ do fundo, armazenado apenas com dígitos (sem máscara).
     * Ex: "11728688000147".
     */
    @Column(length = 14)
    @Schema(description = "CNPJ do fundo (apenas dígitos)", example = "11728688000147", maxLength = 14)
    private String cnpj;

    /**
     * Tipo do fundo (Tijolo, Papel, Híbrido ou Fundo de Fundos).
     * Armazenado como VARCHAR no banco (mais legível que o ordinal).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Schema(description = "Tipo do fundo", example = "TIJOLO")
    private TipoFundo tipo;

    /**
     * Segmento de atuação do fundo (Logística, Shopping, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Schema(description = "Segmento de atuação", example = "LOGISTICA")
    private Segmento segmento;

    /**
     * Indica se o fundo está ativo na carteira do usuário.
     * Usado para "soft delete" — ao invés de apagar, marcamos como inativo.
     */
    @Column(nullable = false)
    @Builder.Default
    @Schema(description = "Indica se o fundo está ativo na carteira", example = "true")
    private Boolean ativo = true;

    /**
     * Data e hora de criação do registro, preenchida automaticamente
     * pelo Hibernate no primeiro INSERT.
     */
    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    @Schema(description = "Data de criação do registro", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataCriacao;

    /**
     * Data e hora da última atualização, atualizada automaticamente
     * pelo Hibernate a cada UPDATE.
     */
    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    @Schema(description = "Data da última atualização", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataAtualizacao;
}
