package com.renlip.fiis.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.renlip.fiis.domain.model.Operacao;

/**
 * Repositório de acesso a dados da entidade {@link Operacao}.
 */
@Repository
public interface OperacaoRepository extends JpaRepository<Operacao, Long> {

    /**
     * Lista todas as operações de um determinado fundo, ordenadas
     * pela data da operação (mais recente primeiro).
     *
     * @param fundoId ID do fundo
     * @return lista de operações
     */
    List<Operacao> findByFundoIdOrderByDataOperacaoDesc(Long fundoId);

    /**
     * Verifica se existem operações associadas ao fundo informado.
     * Útil para impedir exclusão de fundos com operações registradas.
     *
     * @param fundoId ID do fundo
     * @return {@code true} se houver alguma operação
     */
    boolean existsByFundoId(Long fundoId);

    /**
     * Calcula a posição atual (saldo de cotas) em um fundo.
     *
     * <p>Soma o número de cotas compradas e subtrai o número vendido.
     * Retorna 0 se não houver operações.</p>
     *
     * @param fundoId ID do fundo
     * @return saldo de cotas atual (sempre ≥ 0 se a operação foi válida)
     */
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN o.tipo = 'COMPRA'
                                 THEN o.quantidade
                                 ELSE -o.quantidade END), 0)
        FROM Operacao o
        WHERE o.fundo.id = :fundoId
        """)
    Integer calcularPosicaoAtual(@Param("fundoId") Long fundoId);

    /**
     * Calcula a posição atual em um fundo, <b>ignorando</b> a operação
     * cujo ID é informado. Útil para validar alterações em operações
     * existentes: simulamos a posição sem o efeito da operação atual.
     *
     * @param fundoId       ID do fundo
     * @param excluindoId   ID da operação a ser ignorada no cálculo
     * @return saldo de cotas desconsiderando a operação informada
     */
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN o.tipo = 'COMPRA'
                                 THEN o.quantidade
                                 ELSE -o.quantidade END), 0)
        FROM Operacao o
        WHERE o.fundo.id = :fundoId
          AND o.id <> :excluindoId
        """)
    Integer calcularPosicaoExcluindo(@Param("fundoId") Long fundoId,
                                     @Param("excluindoId") Long excluindoId);
}
