package com.renlip.fiis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.renlip.fiis.domain.entity.Operacao;

/**
 * Repositório de acesso a dados da entidade {@link Operacao}.
 */
@Repository
public interface OperacaoRepository extends JpaRepository<Operacao, Long> {

    /**
     * Lista todas as operações do usuário informado.
     */
    List<Operacao> findByUsuarioId(Long usuarioId);

    /**
     * Busca uma operação pelo ID garantindo que pertence ao usuário informado.
     */
    Optional<Operacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    /**
     * Lista todas as operações de um determinado fundo, ordenadas
     * pela data da operação (mais recente primeiro).
     */
    List<Operacao> findByFundoIdOrderByDataOperacaoDesc(Long fundoId);

    /**
     * Verifica se existem operações associadas ao fundo informado.
     * Útil para impedir exclusão de fundos com operações registradas.
     */
    boolean existsByFundoId(Long fundoId);

    /**
     * Calcula a posição atual (saldo de cotas) em um fundo.
     *
     * <p>Soma o número de cotas compradas e subtrai o número vendido.
     * Retorna 0 se não houver operações.</p>
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
