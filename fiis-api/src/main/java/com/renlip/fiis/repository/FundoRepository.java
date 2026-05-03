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
     * Lista todos os fundos ativos do sistema (ativo = true), sem filtro de
     * usuário. Usado apenas pelo perfil ADMIN.
     */
    List<Fundo> findByAtivoTrue();

    /**
     * Busca um fundo pelo ID garantindo que pertence ao usuário informado.
     * Retorna {@link Optional#empty()} se o fundo não existir OU pertencer a
     * outro usuário (evita vazar existência de recursos alheios).
     */
    Optional<Fundo> findByIdAndUsuarioId(Long id, Long usuarioId);

    /**
     * Lista todos os fundos de um usuário.
     */
    List<Fundo> findByUsuarioId(Long usuarioId);

    /**
     * Lista os fundos ativos (ativo = true) de um usuário.
     */
    List<Fundo> findByUsuarioIdAndAtivoTrue(Long usuarioId);

    /**
     * Verifica se o usuário já tem um fundo cadastrado com o ticker informado.
     *
     * <p>A unicidade do ticker é <b>por usuário</b>: dois usuários distintos
     * podem cadastrar o mesmo ticker (ex: HGLG11) em suas carteiras.</p>
     */
    boolean existsByUsuarioIdAndTicker(Long usuarioId, String ticker);
}
