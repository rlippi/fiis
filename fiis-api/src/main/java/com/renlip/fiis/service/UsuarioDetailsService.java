package com.renlip.fiis.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.renlip.fiis.config.security.JwtUserDetails;
import com.renlip.fiis.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Carrega dados do usuário para o Spring Security durante a autenticação.
 *
 * <p>Chamado pelo {@code AuthenticationManager} ao validar as credenciais do
 * login: recebe o e-mail, busca o {@code Usuario} no banco e o envelopa num
 * {@link JwtUserDetails} que o Spring Security entende.</p>
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
            .map(JwtUserDetails::new)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }
}
