package com.renlip.fiis.config.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.renlip.fiis.domain.entity.Usuario;

import lombok.Getter;

/**
 * Adapter que converte um {@link Usuario} do domínio em um {@link UserDetails}
 * que o Spring Security entende.
 *
 * <p>Isolar a implementação aqui mantém a entidade de domínio livre de
 * dependências do Spring Security.</p>
 */
@Getter
public class JwtUserDetails implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";

    private final Usuario usuario;

    private final Collection<? extends GrantedAuthority> authorities;

    public JwtUserDetails(final Usuario usuario) {
        this.usuario = usuario;
        this.authorities = List.of(new SimpleGrantedAuthority(ROLE_PREFIX + usuario.getPerfil().name()));
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public String getPassword() {
        return usuario.getSenha();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(usuario.getAtivo());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
