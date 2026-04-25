package com.renlip.fiis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.renlip.fiis.domain.entity.Usuario;

/**
 * Repositório JPA para {@link Usuario}.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuário pelo e-mail (case-sensitive).
     *
     * @param email e-mail do usuário
     * @return {@link Optional} com o usuário se encontrado
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica se existe usuário com o e-mail informado.
     *
     * @param email e-mail a verificar
     * @return {@code true} se existir
     */
    boolean existsByEmail(String email);

    /**
     * Retorna todos os usuários com {@code ativo = true}.
     *
     * <p>Usado pelo job agendado de importação de cotações para iterar apenas
     * sobre usuários que ainda podem autenticar — usuários inativos não
     * deveriam gastar requests da BRAPI.</p>
     *
     * @return lista de usuários ativos (pode ser vazia)
     */
    List<Usuario> findByAtivoTrue();
}
