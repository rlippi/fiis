package com.renlip.fiis.domain.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.renlip.fiis.domain.model.Provento;

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
}
