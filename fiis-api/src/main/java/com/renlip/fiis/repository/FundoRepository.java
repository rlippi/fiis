package com.renlip.fiis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.renlip.fiis.domain.entity.Fundo;

/**
 * Repositório de acesso a dados da entidade {@link Fundo}.
 *
 * <p>Estende {@link JpaRepository} e ganha automaticamente métodos como:
 * <ul>
 *   <li>{@code save(entidade)} - insere ou atualiza</li>
 *   <li>{@code findById(id)} - busca por ID</li>
 *   <li>{@code findAll()} - lista todos</li>
 *   <li>{@code deleteById(id)} - remove por ID</li>
 *   <li>{@code count()} - conta total de registros</li>
 * </ul>
 * </p>
 *
 * <p>Os métodos personalizados abaixo são criados pelo Spring Data JPA
 * automaticamente, apenas pelo nome — não precisa implementar nada.
 * Essa feature se chama <b>derived query</b>.</p>
 */
@Repository
public interface FundoRepository extends JpaRepository<Fundo, Long> {

    /**
     * Busca um fundo pelo seu ticker (código de negociação).
     *
     * <p>O ticker é único, então retorna no máximo um resultado.
     * Retorna {@link Optional#empty()} se não encontrar.</p>
     *
     * @param ticker código do fundo (ex: "HGLG11")
     * @return {@link Optional} contendo o fundo, se existir
     */
    Optional<Fundo> findByTicker(String ticker);

    /**
     * Verifica se já existe um fundo cadastrado com o ticker informado.
     *
     * <p>Útil para validações de duplicidade antes de cadastrar um novo fundo.</p>
     *
     * @param ticker código do fundo (ex: "HGLG11")
     * @return {@code true} se o ticker já existir no banco
     */
    boolean existsByTicker(String ticker);

    /**
     * Lista todos os fundos ativos na carteira (ativo = true).
     *
     * @return lista de fundos ativos
     */
    List<Fundo> findByAtivoTrue();
}
