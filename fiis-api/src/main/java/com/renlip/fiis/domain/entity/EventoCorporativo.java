package com.renlip.fiis.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.renlip.fiis.domain.enumeration.TipoEventoCorporativo;

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
 * Entidade que representa um evento corporativo de um FII — bonificação,
 * desdobramento ou grupamento.
 *
 * <p>Esses eventos alteram a quantidade de cotas do investidor sem serem
 * operações de compra/venda. O preço médio é ajustado automaticamente
 * para que o custo total da posição se preserve.</p>
 *
 * @see TipoEventoCorporativo
 */
@Entity
@Table(name = "evento_corporativo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Schema(description = "Evento corporativo (bonificação, desdobramento ou grupamento)")
public class EventoCorporativo {

    /**
     * Identificador único gerado pelo banco.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único", example = "1")
    private Long id;

    /**
     * Fundo a que o evento se refere.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fundo_id", nullable = false)
    @Schema(description = "Fundo do evento")
    private Fundo fundo;

    /**
     * Tipo do evento (BONIFICACAO, DESDOBRAMENTO ou GRUPAMENTO).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(description = "Tipo do evento", example = "DESDOBRAMENTO")
    private TipoEventoCorporativo tipo;

    /**
     * Data em que o evento ocorreu (data ex — a partir dela a quantidade é alterada).
     */
    @Column(nullable = false)
    @Schema(description = "Data do evento (data ex)", example = "2026-04-10")
    private LocalDate data;

    /**
     * Fator do evento.
     * <ul>
     *   <li><b>BONIFICACAO:</b> proporção (ex: 0.10 = +10%)</li>
     *   <li><b>DESDOBRAMENTO:</b> multiplicador (ex: 10 = 1:10)</li>
     *   <li><b>GRUPAMENTO:</b> divisor (ex: 10 = 10:1)</li>
     * </ul>
     */
    @Column(nullable = false, precision = 10, scale = 6)
    @Schema(description = "Fator do evento (ver documentação do tipo)", example = "10")
    private BigDecimal fator;

    /**
     * Descrição livre do evento (opcional).
     */
    @Column(length = 255)
    @Schema(description = "Descrição do evento", example = "Desdobramento 1:10 aprovado em AGE")
    private String descricao;

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
