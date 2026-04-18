package com.renlip.fiis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.renlip.fiis.config.security.JwtUserDetails;
import com.renlip.fiis.domain.dto.TokenResponse;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.vo.CredencialVO;
import com.renlip.fiis.support.JwtSupport;

import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela autenticação de usuários.
 *
 * <p>Delega a validação das credenciais ao {@link AuthenticationManager} do Spring Security
 * (que por sua vez chama {@code UsuarioDetailsService} + {@code PasswordEncoder}) e gera o
 * token JWT através de {@link JwtSupport}.</p>
 */
@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private static final String TIPO_BEARER = "Bearer";

    private final AuthenticationManager authenticationManager;

    private final JwtSupport jwtSupport;

    @Value("${fiis.jwt.ttl-millis}")
    private long ttlMillis;

    /**
     * Autentica as credenciais e devolve um token JWT pronto para uso.
     *
     * @param credencial e-mail e senha
     * @return {@link TokenResponse} com o token e metadados
     */
    public TokenResponse login(final CredencialVO credencial) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(credencial.email(), credencial.senha()));

        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        Usuario usuario = userDetails.getUsuario();

        String token = jwtSupport.gerarToken(userDetails);

        return new TokenResponse(
            token,
            TIPO_BEARER,
            usuario.getNome(),
            usuario.getPerfil(),
            ttlMillis
        );
    }
}
