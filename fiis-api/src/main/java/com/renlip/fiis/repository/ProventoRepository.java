package com.renlip.fiis.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.renlip.fiis.domain.entity.Provento;

/**
 * Repositório de acesso a dados da entidade {@link Provento}.
 */
@Repository
public interface ProventoRepository extends JpaRepository<Provento, Long> {

    /**
     * Lista todos os proventos do usuário informado.
     */
    List<Provento> findByUsuarioId(Long usuarioId);

    /**
     * Busca um provento pelo ID garantindo que pertence ao usuário informado.
     */
    Optional<Provento> findByIdAndUsuarioId(Long id, Long usuarioId);

    /**
     * Lista os proventos de um fundo, do mais recente ao mais antigo.
     */
    List<Provento> findByFundoIdOrderByDataReferenciaDesc(Long fundoId);

    /**
     * Lista os proventos pagos dentro do intervalo informado (inclusivo), sem filtro
     * de usuário. Usado em relatórios administrativos.
     */
    List<Provento> findByDataPagamentoBetweenOrderByDataPagamentoDesc(LocalDate inicio, LocalDate fim);

    /**
     * Lista os proventos de um usuário, pagos dentro do intervalo informado (inclusivo).
     */
    List<Provento> findByUsuarioIdAndDataPagamentoBetweenOrderByDataPagamentoDesc(
        Long usuarioId, LocalDate inicio, LocalDate fim);

    /**
     * Verifica se existem proventos vinculados ao fundo.
     */
    boolean existsByFundoId(Long fundoId);

    /**
     * Agrega os proventos (de todos os usuários) por ano/mês — usado por ADMIN.
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
     * Agrega os proventos de um usuário específico por ano/mês.
     */
    @Query("""
        SELECT YEAR(p.dataPagamento),
               MONTH(p.dataPagamento),
               SUM(p.valorPorCota * p.quantidadeCotas),
               COUNT(p)
        FROM Provento p
        WHERE p.usuario.id = :usuarioId
        GROUP BY YEAR(p.dataPagamento), MONTH(p.dataPagamento)
        ORDER BY YEAR(p.dataPagamento) DESC, MONTH(p.dataPagamento) DESC
        """)
    List<Object[]> agregarRendaMensalPorUsuario(@Param("usuarioId") Long usuarioId);

    /**
     * Agrega os proventos (de todos os usuários) por fundo — usado por ADMIN.
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

    /**
     * Agrega os proventos de um usuário específico por fundo.
     */
    @Query("""
        SELECT p.fundo.id,
               SUM(p.valorPorCota * p.quantidadeCotas),
               COUNT(p)
        FROM Provento p
        WHERE p.usuario.id = :usuarioId
        GROUP BY p.fundo.id
        ORDER BY SUM(p.valorPorCota * p.quantidadeCotas) DESC
        """)
    List<Object[]> agregarRendaPorFundoPorUsuario(@Param("usuarioId") Long usuarioId);
}
