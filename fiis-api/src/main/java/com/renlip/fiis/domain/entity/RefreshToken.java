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
 * Refresh token emitido no login/signup e rotacionado em
 * {@code /api/auth/refresh}. Single-use por design — tentar consumir o mesmo
 * token mais de uma vez dispara <i>reuse detection</i> e invalida todos os
 * refresh tokens ativos do usuário (sinal de possível roubo).
 *
 * <p>O token enviado ao cliente é um valor opaco gerado com {@code SecureRandom}
 * e codificado em base64-url-safe. <b>Apenas o hash SHA-256 é persistido</b>:
 * comprometimento do banco não permite que um atacante use os tokens
 * existentes — ele teria os hashes, não os raws.</p>
 *
 * <p>Distinção {@link #usedAt} vs {@link #revokedAt}:
 * <ul>
 *   <li>{@code usedAt}: token foi consumido em {@code /refresh} (rotação
 *       legítima). Tentar reusar é tratado como ataque.</li>
 *   <li>{@code revokedAt}: token foi explicitamente revogado em
 *       {@code /logout}. Tentar reusar simplesmente falha sem escalar para
 *       reuse detection — o usuário desligou aquela sessão de propósito.</li>
 * </ul>
 *
 * <p>{@link #replacedBy} mantém a cadeia de rotação: ao consumir, criamos um
 * novo token e apontamos {@code replacedBy} do antigo para o novo. Útil para
 * auditoria e investigação ("este token vazou — quais vieram depois dele?").</p>
 */
@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuário dono do refresh token.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * SHA-256 hex (64 chars) do token raw enviado ao cliente. {@code unique}
     * garante que cada token é único globalmente.
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /**
     * Instante a partir do qual o token deixa de valer.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Instante em que o token foi consumido em {@code /refresh}. Nulo enquanto
     * o token ainda pode ser rotacionado.
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * Instante em que o token foi explicitamente revogado (logout). Distinto
     * de {@link #usedAt} para evitar reuse detection acidental quando o usuário
     * desliga uma sessão.
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Token sucessor na cadeia de rotação. Preenchido quando este token é
     * consumido e um novo é emitido em sequência.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_id")
    private RefreshToken replacedBy;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    /**
     * Token está apto a ser rotacionado: não usado, não revogado e dentro
     * da validade.
     */
    public boolean podeRotacionar() {
        return usedAt == null && revokedAt == null
            && expiresAt != null && expiresAt.isAfter(LocalDateTime.now());
    }

    public void marcarComoUsado(final RefreshToken sucessor) {
        this.usedAt = LocalDateTime.now();
        this.replacedBy = sucessor;
    }

    public void revogar() {
        this.revokedAt = LocalDateTime.now();
    }
}
