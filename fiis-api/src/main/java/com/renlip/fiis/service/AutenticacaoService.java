package com.renlip.fiis.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.config.ResetTokenProperties;
import com.renlip.fiis.config.security.JwtUserDetails;
import com.renlip.fiis.domain.dto.TokenResponse;
import com.renlip.fiis.domain.entity.ResetToken;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.MensagemEnum;
import com.renlip.fiis.domain.enumeration.Perfil;
import com.renlip.fiis.domain.vo.CredencialVO;
import com.renlip.fiis.domain.vo.EsqueciSenhaVO;
import com.renlip.fiis.domain.vo.ResetSenhaVO;
import com.renlip.fiis.domain.vo.SignupVO;
import com.renlip.fiis.exception.RegraNegocioException;
import com.renlip.fiis.repository.ResetTokenRepository;
import com.renlip.fiis.repository.UsuarioRepository;
import com.renlip.fiis.support.JwtSupport;
import com.renlip.fiis.support.RateLimitSupport;

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

    private final RateLimitSupport rateLimit;

    private final ResetTokenRepository resetTokenRepository;

    private final ResetTokenProperties resetTokenProperties;

    private final EmailService emailService;

    @Value("${fiis.jwt.ttl-millis}")
    private long ttlMillis;

    /**
     * Autentica as credenciais e devolve um token JWT pronto para uso.
     *
     * <p>Cada tentativa (válida ou não) consome 1 token do bucket associado ao
     * e-mail; se o limite for excedido, dispara HTTP 429 antes mesmo de validar
     * a senha. Impede brute force em uma conta específica.</p>
     *
     * @param credencial e-mail e senha
     * @return {@link TokenResponse} com o token e metadados
     */
    public TokenResponse login(final CredencialVO credencial) {
        rateLimit.consumirLogin(credencial.email());

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
     * <p>Cada chamada consome 1 token do bucket associado ao IP do cliente; se
     * o limite for excedido, dispara HTTP 429. Impede que bots criem contas em
     * massa a partir de um mesmo IP.</p>
     *
     * @param signup dados do novo usuário
     * @param ip     IP do cliente, usado como chave do rate limit
     * @return {@link TokenResponse} com o token JWT do usuário recém-criado
     * @throws RegraNegocioException se já existir usuário com o e-mail informado
     */
    @Transactional
    public TokenResponse signup(final SignupVO signup, final String ip) {
        rateLimit.consumirSignup(ip);

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

    /**
     * Inicia o fluxo de "esqueci minha senha".
     *
     * <p>Consome 1 token do bucket por e-mail (rate limit) e, se o e-mail
     * pertencer a um usuário ativo, gera um novo {@link ResetToken},
     * invalida quaisquer tokens anteriores ainda válidos do mesmo usuário e
     * dispara o email de redefinição. <b>A resposta é sempre a mesma</b> (o
     * controller devolve 200), existindo ou não o e-mail — evita enumeração
     * de contas cadastradas.</p>
     */
    @Transactional
    public void forgotPassword(final EsqueciSenhaVO esqueci) {
        rateLimit.consumirForgotPassword(esqueci.email());

        usuarioRepository.findByEmail(esqueci.email())
            .filter(Usuario::getAtivo)
            .ifPresent(this::emitirTokenEEnviarEmail);
    }

    /**
     * Conclui o fluxo: valida o token e troca a senha.
     *
     * @throws RegraNegocioException (FII0023) se o token não existir,
     *         estiver expirado ou já tiver sido consumido
     */
    @Transactional
    public void resetPassword(final ResetSenhaVO reset) {
        ResetToken token = resetTokenRepository.findByToken(reset.token())
            .filter(ResetToken::isValido)
            .orElseThrow(() -> new RegraNegocioException(MensagemEnum.TOKEN_RESET_INVALIDO));

        Usuario usuario = token.getUsuario();
        usuario.setSenha(passwordEncoder.encode(reset.novaSenha()));
        usuarioRepository.save(usuario);

        token.marcarComoUsado();
        resetTokenRepository.save(token);
    }

    private void emitirTokenEEnviarEmail(final Usuario usuario) {
        LocalDateTime agora = LocalDateTime.now();
        resetTokenRepository.invalidarTokensAtivos(usuario.getId(), agora);

        long ttl = resetTokenProperties.ttlMinutos();
        ResetToken novo = ResetToken.builder()
            .usuario(usuario)
            .token(UUID.randomUUID().toString())
            .expiresAt(agora.plusMinutes(ttl))
            .build();
        novo = resetTokenRepository.save(novo);

        emailService.enviarResetSenha(usuario.getEmail(), usuario.getNome(),
            novo.getToken(), ttl);
    }
}
