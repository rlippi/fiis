package com.renlip.fiis.support;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.renlip.fiis.config.security.JwtUserDetails;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.Perfil;

/**
 * Helper que expõe o {@link Usuario} autenticado no contexto atual.
 *
 * <p>Encapsula o acesso ao {@link SecurityContextHolder} e ao {@link JwtUserDetails},
 * para que os services do domínio não precisem conhecer essas classes do Spring
 * Security diretamente.</p>
 */
@Component
public class UsuarioLogadoSupport {

    /**
     * Retorna o {@link Usuario} autenticado no contexto atual.
     *
     * @return usuário logado
     * @throws IllegalStateException se não houver autenticação — só pode ocorrer
     *         se o método for chamado em uma rota pública; as rotas autenticadas
     *         garantem autenticação antes de atingir o service.
     */
    public Usuario getUsuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof JwtUserDetails details)) {
            throw new IllegalStateException("Nenhum usuário autenticado no contexto de segurança.");
        }
        return details.getUsuario();
    }

    /**
     * Atalho para {@code getUsuarioAtual().getId()}.
     */
    public Long getUsuarioIdAtual() {
        return getUsuarioAtual().getId();
    }

    /**
     * Indica se o usuário autenticado tem perfil {@link Perfil#ADMIN}.
     * Usado para decidir entre "ver apenas meus dados" (USER) e "ver todos" (ADMIN).
     */
    public boolean isAdmin() {
        return getUsuarioAtual().getPerfil() == Perfil.ADMIN;
    }
}
