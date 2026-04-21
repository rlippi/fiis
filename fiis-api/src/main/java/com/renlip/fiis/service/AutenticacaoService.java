package com.renlip.fiis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.config.security.JwtUserDetails;
import com.renlip.fiis.domain.dto.TokenResponse;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.MensagemEnum;
import com.renlip.fiis.domain.enumeration.Perfil;
import com.renlip.fiis.domain.vo.CredencialVO;
import com.renlip.fiis.domain.vo.SignupVO;
import com.renlip.fiis.exception.RegraNegocioException;
import com.renlip.fiis.repository.UsuarioRepository;
import com.renlip.fiis.support.JwtSupport;

import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela autenticação e cadastro de usuários.
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

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

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

    /**
     * Cria uma nova conta com perfil {@link Perfil#USER} e retorna um token JWT
     * já autenticado (auto-login após cadastro).
     *
     * <p><b>Regra de negócio:</b> e-mail deve ser único no banco.</p>
     *
     * @param signup dados do novo usuário
     * @return {@link TokenResponse} com o token JWT do usuário recém-criado
     * @throws RegraNegocioException se já existir usuário com o e-mail informado
     */
    @Transactional
    public TokenResponse signup(final SignupVO signup) {
        if (usuarioRepository.existsByEmail(signup.email())) {
            throw new RegraNegocioException(MensagemEnum.EMAIL_JA_CADASTRADO, signup.email());
        }

        Usuario usuario = Usuario.builder()
            .nome(signup.nome())
            .email(signup.email())
            .senha(passwordEncoder.encode(signup.senha()))
            .perfil(Perfil.USER)
            .ativo(true)
            .build();

        Usuario salvo = usuarioRepository.save(usuario);

        JwtUserDetails userDetails = new JwtUserDetails(salvo);
        String token = jwtSupport.gerarToken(userDetails);

        return new TokenResponse(
            token,
            TIPO_BEARER,
            salvo.getNome(),
            salvo.getPerfil(),
            ttlMillis
        );
    }
}
