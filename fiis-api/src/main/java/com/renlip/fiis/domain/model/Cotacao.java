package com.renlip.fiis.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade que representa a cotação histórica de um FII em uma determinada data.
 *
 * <p>Armazena os preços diários (abertura, fechamento, mínimo, máximo) e o
 * volume negociado. Permite cálculos como valor atual da carteira, variação
 * patrimonial, P/VP e Dividend Yield sobre preço de mercado.</p>
 *
 * <p><b>Unicidade:</b> não pode existir duas cotações para o mesmo fundo na
 * mesma data — garantido por {@link UniqueConstraint}.</p>
 */
@Entity
@Table(
    name = "cotacao",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_cotacao_fundo_data",
        columnNames = {"fundo_id", "data"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Schema(description = "Cotação histórica de um FII")
public class Cotacao {

    /**
     * Identificador único gerado pelo banco.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único da cotação", example = "1")
    private Long id;

    /**
     * Fundo a que a cotação se refere.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fundo_id", nullable = false)
    @Schema(description = "Fundo da cotação")
    private Fundo fundo;

    /**
     * Data de negociação (pregão) da cotação.
     */
    @Column(nullable = false)
    @Schema(description = "Data da cotação", example = "2026-04-17")
    private LocalDate data;

    /**
     * Preço de fechamento do pregão (campo principal, obrigatório).
     */
    @Column(name = "preco_fechamento", nullable = false, precision = 15, scale = 4)
    @Schema(description = "Preço de fechamento (R$)", example = "160.25")
    private BigDecimal precoFechamento;

    /**
     * Preço de abertura do pregão (opcional).
     */
    @Column(name = "preco_abertura", precision = 15, scale = 4)
    @Schema(description = "Preço de abertura (R$)", example = "158.90")
    private BigDecimal precoAbertura;

    /**
     * Menor preço negociado no pregão (opcional).
     */
    @Column(name = "preco_minimo", precision = 15, scale = 4)
    @Schema(description = "Menor preço do pregão (R$)", example = "158.00")
    private BigDecimal precoMinimo;

    /**
     * Maior preço negociado no pregão (opcional).
     */
    @Column(name = "preco_maximo", precision = 15, scale = 4)
    @Schema(description = "Maior preço do pregão (R$)", example = "161.50")
    private BigDecimal precoMaximo;

    /**
     * Volume financeiro negociado (opcional, em reais).
     */
    @Column(precision = 18, scale = 2)
    @Schema(description = "Volume financeiro negociado (R$)", example = "1250000.00")
    private BigDecimal volume;

    /**
     * Data e hora de criação do registro (automático).
     */
    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    @Schema(description = "Data de criação do registro", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataCriacao;

    /**
     * Data e hora da última atualização (automático).
     */
    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    @Schema(description = "Data da última atualização", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataAtualizacao;
}
