package com.renlip.fiis.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.renlip.fiis.domain.entity.RefreshToken;

/**
 * Repositório JPA de {@link RefreshToken}.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca um refresh token pelo hash do valor raw — caminho único de validação.
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Revoga em massa todos os refresh tokens não-revogados de um usuário.
     *
     * <p>Usado pelo fluxo de <i>reuse detection</i>: ao detectar que um token
     * já consumido foi tentado novamente (sinal de possível roubo),
     * invalidamos todas as sessões ativas para forçar re-login. Tokens já
     * revogados (logout explícito) não são tocados.</p>
     *
     * @param usuarioId usuário-alvo
     * @param revokedAt instante a registrar como {@code revoked_at}
     * @return número de linhas afetadas (útil para logs/métricas)
     */
    @Modifying
    @Query("update RefreshToken rt set rt.revokedAt = :revokedAt "
         + "where rt.usuario.id = :usuarioId and rt.revokedAt is null")
    int revogarTodosDoUsuario(@Param("usuarioId") Long usuarioId,
                              @Param("revokedAt") LocalDateTime revokedAt);
}
