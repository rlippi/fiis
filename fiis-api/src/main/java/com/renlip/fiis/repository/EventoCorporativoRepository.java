package com.renlip.fiis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.renlip.fiis.domain.entity.EventoCorporativo;

/**
 * Repositório de acesso a dados da entidade {@link EventoCorporativo}.
 */
@Repository
public interface EventoCorporativoRepository extends JpaRepository<EventoCorporativo, Long> {

    /**
     * Lista os eventos corporativos de um fundo ordenados
     * cronologicamente (do mais recente ao mais antigo).
     *
     * @param fundoId ID do fundo
     * @return lista de eventos
     */
    List<EventoCorporativo> findByFundoIdOrderByDataDesc(Long fundoId);

    /**
     * Lista os eventos de um fundo ordenados cronologicamente crescente
     * (do mais antigo ao mais recente). Útil para processar o efeito na posição.
     *
     * @param fundoId ID do fundo
     * @return lista de eventos
     */
    List<EventoCorporativo> findByFundoIdOrderByDataAsc(Long fundoId);

    /**
     * Verifica se existem eventos corporativos vinculados ao fundo.
     *
     * @param fundoId ID do fundo
     * @return {@code true} se houver pelo menos um evento
     */
    boolean existsByFundoId(Long fundoId);
}
