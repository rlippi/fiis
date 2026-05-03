package com.renlip.fiis.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.renlip.fiis.domain.entity.Cotacao;

/**
 * Repositório de acesso a dados da entidade {@link Cotacao}.
 */
@Repository
public interface CotacaoRepository extends JpaRepository<Cotacao, Long> {

    /**
     * Lista todas as cotações do usuário informado.
     */
    List<Cotacao> findByUsuarioId(Long usuarioId);

    /**
     * Busca uma cotação pelo ID garantindo que pertence ao usuário informado.
     */
    Optional<Cotacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    /**
     * Busca a cotação de um fundo em uma data específica.
     */
    Optional<Cotacao> findByFundoIdAndData(Long fundoId, LocalDate data);

    /**
     * Verifica se já existe cotação para o fundo na data informada.
     * Usado para evitar duplicatas no cadastro.
     */
    boolean existsByFundoIdAndData(Long fundoId, LocalDate data);

    /**
     * Retorna todas as cotações de um fundo ordenadas da mais recente
     * para a mais antiga.
     */
    List<Cotacao> findByFundoIdOrderByDataDesc(Long fundoId);

    /**
     * Retorna a cotação mais recente (último pregão) de um fundo.
     */
    Optional<Cotacao> findFirstByFundoIdOrderByDataDesc(Long fundoId);
}
