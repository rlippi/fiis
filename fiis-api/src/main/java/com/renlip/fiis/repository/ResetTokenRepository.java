package com.renlip.fiis.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.renlip.fiis.domain.entity.ResetToken;

/**
 * Repositório da entidade {@link ResetToken}.
 */
@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {

    /**
     * Busca pelo valor do token (opaco). O caminho principal do reset.
     */
    Optional<ResetToken> findByToken(String token);

    /**
     * Marca como usados todos os tokens ainda válidos de um usuário.
     * Executado antes de emitir um novo token para garantir apenas um
     * reset ativo por vez.
     *
     * @return quantidade de tokens invalidados
     */
    @Modifying
    @Query("""
        UPDATE ResetToken t
           SET t.usedAt = :agora
         WHERE t.usuario.id = :usuarioId
           AND t.usedAt IS NULL
           AND t.expiresAt > :agora
        """)
    int invalidarTokensAtivos(@Param("usuarioId") Long usuarioId, @Param("agora") LocalDateTime agora);
}
