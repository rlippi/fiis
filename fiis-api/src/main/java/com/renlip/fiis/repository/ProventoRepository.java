package com.renlip.fiis.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.renlip.fiis.domain.entity.Provento;

/**
 * Repositório de acesso a dados da entidade {@link Provento}.
 */
@Repository
public interface ProventoRepository extends JpaRepository<Provento, Long> {

    /**
     * Lista os proventos de um fundo, do mais recente ao mais antigo.
     *
     * @param fundoId ID do fundo
     * @return lista de proventos do fundo
     */
    List<Provento> findByFundoIdOrderByDataReferenciaDesc(Long fundoId);

    /**
     * Lista os proventos pagos dentro do intervalo informado (inclusivo).
     *
     * <p>Útil para relatórios de renda passiva por período.</p>
     *
     * @param inicio data de pagamento inicial (inclusive)
     * @param fim    data de pagamento final (inclusive)
     * @return lista de proventos no intervalo
     */
    List<Provento> findByDataPagamentoBetweenOrderByDataPagamentoDesc(LocalDate inicio, LocalDate fim);

    /**
     * Verifica se existem proventos vinculados ao fundo.
     *
     * @param fundoId ID do fundo
     * @return {@code true} se houver pelo menos um provento
     */
    boolean existsByFundoId(Long fundoId);

    /**
     * Agrega os proventos por ano/mês (baseado em {@code dataPagamento})
     * somando o valor total recebido e contando quantos proventos foram pagos.
     *
     * <p>Resultado em {@code Object[]}:
     * <ol>
     *   <li>{@code [0]} = ano (Integer);</li>
     *   <li>{@code [1]} = mês (Integer);</li>
     *   <li>{@code [2]} = soma dos valores (BigDecimal);</li>
     *   <li>{@code [3]} = quantidade de proventos (Long).</li>
     * </ol>
     * </p>
     *
     * @return lista ordenada do mês mais recente para o mais antigo
     */
    @Query("""
        SELECT YEAR(p.dataPagamento),
               MONTH(p.dataPagamento),
               SUM(p.valorPorCota * p.quantidadeCotas),
               COUNT(p)
        FROM Provento p
        GROUP BY YEAR(p.dataPagamento), MONTH(p.dataPagamento)
        ORDER BY YEAR(p.dataPagamento) DESC, MONTH(p.dataPagamento) DESC
        """)
    List<Object[]> agregarRendaMensal();

    /**
     * Agrega os proventos por fundo, somando o total recebido.
     *
     * <p>Resultado em {@code Object[]}:
     * <ol>
     *   <li>{@code [0]} = ID do fundo (Long);</li>
     *   <li>{@code [1]} = soma dos valores (BigDecimal);</li>
     *   <li>{@code [2]} = quantidade de proventos (Long).</li>
     * </ol>
     * </p>
     *
     * @return lista ordenada do maior recebedor para o menor
     */
    @Query("""
        SELECT p.fundo.id,
               SUM(p.valorPorCota * p.quantidadeCotas),
               COUNT(p)
        FROM Provento p
        GROUP BY p.fundo.id
        ORDER BY SUM(p.valorPorCota * p.quantidadeCotas) DESC
        """)
    List<Object[]> agregarRendaPorFundo();
}
