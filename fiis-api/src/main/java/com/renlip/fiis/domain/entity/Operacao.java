package com.renlip.fiis.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.renlip.fiis.domain.enumeration.TipoOperacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade que representa uma operação (compra ou venda) de cotas de um FII.
 *
 * <p>Cada registro é um evento realizado na corretora em uma data específica,
 * por uma determinada quantidade de cotas a um determinado preço unitário.
 * Pode conter taxas (corretagem e emolumentos).</p>
 *
 * <p><b>Mapeamento no banco:</b> tabela {@code operacao}, com chave
 * estrangeira para {@code fundo}.</p>
 */
@Entity
@Table(name = "operacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Schema(description = "Operação de compra ou venda de cotas de um FII")
public class Operacao {

    /**
     * Identificador único gerado pelo banco.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único da operação", example = "1")
    private Long id;

    /**
     * Fundo relacionado à operação.
     *
     * <p><b>Relacionamento:</b> {@link ManyToOne} — muitas operações pertencem
     * a um fundo. Usa {@link FetchType#LAZY} para evitar carregamento
     * desnecessário em queries.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fundo_id", nullable = false)
    @Schema(description = "Fundo da operação")
    private Fundo fundo;

    /**
     * Tipo da operação (COMPRA ou VENDA).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Schema(description = "Tipo da operação", example = "COMPRA")
    private TipoOperacao tipo;

    /**
     * Data em que a operação foi realizada na corretora.
     * Não pode ser futura.
     */
    @Column(name = "data_operacao", nullable = false)
    @Schema(description = "Data da operação", example = "2026-04-17")
    private LocalDate dataOperacao;

    /**
     * Quantidade de cotas envolvidas na operação. Deve ser maior que zero.
     */
    @Column(nullable = false)
    @Schema(description = "Quantidade de cotas", example = "10")
    private Integer quantidade;

    /**
     * Preço pago/recebido por cota, em reais.
     *
     * <p>Usamos {@link BigDecimal} com precisão adequada para valores
     * monetários — nunca use {@code double} para dinheiro.</p>
     */
    @Column(name = "preco_unitario", nullable = false, precision = 15, scale = 4)
    @Schema(description = "Preço unitário por cota (R$)", example = "150.25")
    private BigDecimal precoUnitario;

    /**
     * Total de taxas (corretagem + emolumentos), em reais.
     * Opcional — se não informado, considera-se zero.
     */
    @Column(precision = 10, scale = 2)
    @Builder.Default
    @Schema(description = "Taxas (corretagem + emolumentos) em R$", example = "0.50")
    private BigDecimal taxas = BigDecimal.ZERO;

    /**
     * Observação livre sobre a operação (opcional).
     */
    @Column(length = 255)
    @Schema(description = "Observação livre", example = "Operação pelo home broker")
    private String observacao;

    /**
     * Data e hora de criação do registro (gerado automaticamente).
     */
    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    @Schema(description = "Data de criação do registro", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataCriacao;

    /**
     * Data e hora da última atualização (atualizada automaticamente).
     */
    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    @Schema(description = "Data da última atualização", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataAtualizacao;

    /**
     * Calcula o valor total da operação.
     *
     * <p>Para <b>compras</b>: quantidade × preço unitário + taxas (valor gasto).<br>
     * Para <b>vendas</b>: quantidade × preço unitário − taxas (valor recebido líquido).</p>
     *
     * @return valor total da operação em reais
     */
    public BigDecimal calcularValorTotal() {
        BigDecimal bruto = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        BigDecimal t = taxas != null ? taxas : BigDecimal.ZERO;
        return tipo == TipoOperacao.COMPRA ? bruto.add(t) : bruto.subtract(t);
    }
}
