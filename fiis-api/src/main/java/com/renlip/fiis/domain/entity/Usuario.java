package com.renlip.fiis.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.renlip.fiis.domain.enumeration.Perfil;

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
 * Entidade que representa um usuário do sistema.
 *
 * <p>O campo {@code senha} armazena o <b>hash BCrypt</b> da senha — nunca o
 * valor em texto plano.</p>
 *
 * <p><b>Mapeamento no banco:</b> tabela {@code usuario}.</p>
 *
 * @see Perfil
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Schema(description = "Usuário autenticável do sistema")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do usuário", example = "1")
    private Long id;

    /**
     * E-mail do usuário — funciona como login. Único no banco.
     */
    @Column(nullable = false, unique = true, length = 150)
    @Schema(description = "E-mail do usuário (usado para login)", example = "ren@example.com", maxLength = 150)
    private String email;

    /**
     * Hash BCrypt da senha. Nunca exposto em responses.
     */
    @Column(nullable = false, length = 100)
    @Schema(description = "Hash BCrypt da senha", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String senha;

    /**
     * Nome completo do usuário (exibido na UI).
     */
    @Column(nullable = false, length = 100)
    @Schema(description = "Nome completo do usuário", example = "Renato Lippi", maxLength = 100)
    private String nome;

    /**
     * Perfil de acesso que define as permissões do usuário.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(description = "Perfil de acesso", example = "USER")
    private Perfil perfil;

    /**
     * Indica se o usuário pode autenticar. Usuários inativos são bloqueados no login.
     */
    @Column(nullable = false)
    @Builder.Default
    @Schema(description = "Indica se o usuário está ativo (pode autenticar)", example = "true")
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    @Schema(description = "Data de criação do registro", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    @Schema(description = "Data da última atualização", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataAtualizacao;
}
