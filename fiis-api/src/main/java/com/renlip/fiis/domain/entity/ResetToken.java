package com.renlip.fiis.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Token temporário de redefinição de senha, emitido pelo endpoint
 * {@code /api/auth/forgot-password} e consumido pelo {@code /api/auth/reset-password}.
 *
 * <p>É single-use: ao consumir (trocar a senha efetivamente), {@code usedAt} é
 * preenchido e o registro fica marcado como gasto. Combinado com a validação
 * de {@code expiresAt}, impede reuso e ataques com tokens vazados.</p>
 */
@Entity
@Table(name = "reset_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuário dono da solicitação de reset.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Valor opaco (UUID) enviado ao usuário por email. Único no banco.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    /**
     * Instante a partir do qual o token não é mais aceito pelo reset.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Instante em que o token foi consumido. Nulo enquanto ainda vale.
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    /**
     * Retorna {@code true} se o token ainda pode ser usado: não consumido e
     * dentro do período de validade.
     */
    public boolean isValido() {
        return usedAt == null && expiresAt != null && expiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Marca o token como consumido no instante atual.
     */
    public void marcarComoUsado() {
        this.usedAt = LocalDateTime.now();
    }
}
