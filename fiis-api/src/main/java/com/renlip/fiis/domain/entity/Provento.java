package com.renlip.fiis.domain.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.renlip.fiis.domain.enumeration.TipoProvento;

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
 * Entidade que representa um provento (rendimento ou amortização) pago por
 * um FII ao cotista em uma determinada data de referência.
 *
 * <p><b>Mapeamento no banco:</b> tabela {@code provento}, com chave estrangeira
 * para {@code fundo}.</p>
 *
 * @see TipoProvento
 */
@Entity
@Table(name = "provento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Schema(description = "Provento pago por um FII (rendimento ou amortização)")
public class Provento {

    /**
     * Identificador único gerado pelo banco.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do provento", example = "1")
    private Long id;

    /**
     * Fundo pagador do provento.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fundo_id", nullable = false)
    @Schema(description = "Fundo pagador")
    private Fundo fundo;

    /**
     * Tipo do provento (RENDIMENTO ou AMORTIZACAO).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_provento", nullable = false, length = 20)
    @Schema(description = "Tipo do provento", example = "RENDIMENTO")
    private TipoProvento tipoProvento;

    /**
     * Data de referência (competência) do provento — normalmente o último dia
     * do mês relativo ao rendimento.
     */
    @Column(name = "data_referencia", nullable = false)
    @Schema(description = "Data de referência (competência)", example = "2026-03-31")
    private LocalDate dataReferencia;

    /**
     * Data em que o provento foi efetivamente pago (creditado ao cotista).
     */
    @Column(name = "data_pagamento", nullable = false)
    @Schema(description = "Data de pagamento", example = "2026-04-15")
    private LocalDate dataPagamento;

    /**
     * Valor pago por cota (R$).
     */
    @Column(name = "valor_por_cota", nullable = false, precision = 15, scale = 6)
    @Schema(description = "Valor pago por cota (R$)", example = "1.12")
    private BigDecimal valorPorCota;

    /**
     * Quantidade de cotas que o investidor possuía na data de referência.
     * Informada manualmente pelo usuário nesta versão (futuramente pode ser
     * calculada a partir das operações).
     */
    @Column(name = "quantidade_cotas", nullable = false)
    @Schema(description = "Quantidade de cotas na data de referência", example = "12")
    private Integer quantidadeCotas;

    /**
     * Observação livre sobre o provento (opcional).
     */
    @Column(length = 255)
    @Schema(description = "Observação livre", example = "Pago conforme calendário mensal")
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
     * Calcula o valor total recebido (quantidade × valor por cota).
     *
     * <p>Arredonda para 2 casas decimais (padrão de valores em reais).</p>
     *
     * @return valor total recebido em R$
     */
    public BigDecimal calcularValorTotal() {
        return valorPorCota
            .multiply(BigDecimal.valueOf(quantidadeCotas))
            .setScale(2, RoundingMode.HALF_UP);
    }
}
