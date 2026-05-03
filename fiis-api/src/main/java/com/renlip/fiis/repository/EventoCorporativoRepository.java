package com.renlip.fiis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.renlip.fiis.domain.entity.EventoCorporativo;

/**
 * Repositório de acesso a dados da entidade {@link EventoCorporativo}.
 */
@Repository
public interface EventoCorporativoRepository extends JpaRepository<EventoCorporativo, Long> {

    /**
     * Lista todos os eventos corporativos do usuário informado.
     */
    List<EventoCorporativo> findByUsuarioId(Long usuarioId);

    /**
     * Busca um evento pelo ID garantindo que pertence ao usuário informado.
     */
    Optional<EventoCorporativo> findByIdAndUsuarioId(Long id, Long usuarioId);

    /**
     * Lista os eventos corporativos de um fundo ordenados
     * cronologicamente (do mais recente ao mais antigo).
     */
    List<EventoCorporativo> findByFundoIdOrderByDataDesc(Long fundoId);

    /**
     * Lista os eventos de um fundo ordenados cronologicamente crescente
     * (do mais antigo ao mais recente). Útil para processar o efeito na posição.
     */
    List<EventoCorporativo> findByFundoIdOrderByDataAsc(Long fundoId);

    /**
     * Verifica se existem eventos corporativos vinculados ao fundo.
     */
    boolean existsByFundoId(Long fundoId);
}
