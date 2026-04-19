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
     * Busca a cotação de um fundo em uma data específica.
     *
     * @param fundoId ID do fundo
     * @param data    data do pregão
     * @return {@link Optional} contendo a cotação, se existir
     */
    Optional<Cotacao> findByFundoIdAndData(Long fundoId, LocalDate data);

    /**
     * Verifica se já existe cotação para o fundo na data informada.
     * Usado para evitar duplicatas no cadastro.
     *
     * @param fundoId ID do fundo
     * @param data    data do pregão
     * @return {@code true} se já existe
     */
    boolean existsByFundoIdAndData(Long fundoId, LocalDate data);

    /**
     * Retorna todas as cotações de um fundo ordenadas da mais recente
     * para a mais antiga.
     *
     * @param fundoId ID do fundo
     * @return lista de cotações
     */
    List<Cotacao> findByFundoIdOrderByDataDesc(Long fundoId);

    /**
     * Retorna a cotação mais recente (último pregão) de um fundo.
     *
     * <p>Usa o Spring Data JPA: {@code findFirst} + {@code OrderBy...Desc}
     * gera {@code SELECT ... LIMIT 1}.</p>
     *
     * @param fundoId ID do fundo
     * @return cotação mais recente ou {@link Optional#empty()} se não houver
     */
    Optional<Cotacao> findFirstByFundoIdOrderByDataDesc(Long fundoId);
}
