package com.renlip.fiis.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.renlip.fiis.config.RefreshTokenProperties;
import com.renlip.fiis.domain.entity.RefreshToken;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.MensagemEnum;
import com.renlip.fiis.exception.RegraNegocioException;
import com.renlip.fiis.repository.RefreshTokenRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsável pelo ciclo de vida do refresh token.
 *
 * <p>Gera tokens raw com {@link SecureRandom} (32 bytes em base64-url-safe),
 * persiste apenas o hash SHA-256 no banco, e expõe três operações principais:
 * <ul>
 *   <li>{@link #emitir(Usuario)}: cria um novo refresh para um usuário (login/signup);</li>
 *   <li>{@link #rotacionar(String)}: consome um refresh válido e devolve o
 *       {@link Usuario} dono + um novo refresh já persistido na cadeia. Detecta
 *       reuse e invalida em massa quando aplicável;</li>
 *   <li>{@link #revogar(String)}: marca um refresh como revogado por logout
 *       (não dispara reuse detection — distingue de uso abusivo via {@code revoked_at}).</li>
 * </ul>
 *
 * <p><b>Por que não persistir o token raw:</b> o cliente recebe o raw uma única
 * vez (na resposta do login/refresh). O servidor guarda só o hash; comprometer
 * o banco não permite que um atacante use os refreshes existentes — ele teria
 * apenas hashes irreversíveis.</p>
 */
@Service
@Slf4j
public class RefreshTokenService {

    private static final int RANDOM_BYTES = 32;

    private static final String HASH_ALGORITHM = "SHA-256";

    private final RefreshTokenRepository refreshTokenRepository;

    private final RefreshTokenProperties properties;

    private final TransactionTemplate revokeTxTemplate;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Constrói o service com um {@link TransactionTemplate} dedicado, configurado
     * em {@code REQUIRES_NEW}, para revogar refreshes em massa quando reuse é
     * detectado. Sem essa transação separada, o {@code RegraNegocioException}
     * lançado em seguida no fluxo de {@link #rotacionar(String)} faria rollback
     * da revogação (default do Spring para RuntimeException) — o oposto do que
     * queremos: a auditoria de roubo precisa persistir.
     */
    public RefreshTokenService(
            final RefreshTokenRepository refreshTokenRepository,
            final RefreshTokenProperties properties,
            final PlatformTransactionManager txManager) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.properties = properties;
        this.revokeTxTemplate = new TransactionTemplate(txManager);
        this.revokeTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * Emite um novo refresh token para o usuário e o devolve já com o
     * {@code rawToken} populado (esse campo é descartado após a transação;
     * é só para o caller enviar ao cliente).
     *
     * @return par (entidade persistida, valor raw a enviar no response)
     */
    @Transactional
    public Emitido emitir(final Usuario usuario) {
        String raw = gerarRawToken();
        RefreshToken token = RefreshToken.builder()
            .usuario(usuario)
            .tokenHash(hash(raw))
            .expiresAt(LocalDateTime.now().plusDays(properties.ttlDias()))
            .build();
        token = refreshTokenRepository.save(token);
        return new Emitido(token, raw);
    }

    /**
     * Consome um refresh token válido, devolve o usuário dono e emite um novo
     * refresh em sequência (rotação). O caller é responsável por gerar o novo
     * access JWT a partir do {@link Usuario} retornado.
     *
     * <p><b>Reuse detection:</b> se o token apresentado já tinha {@code used_at}
     * preenchido, isso é tratado como sinal de roubo — todos os refreshes
     * ativos do usuário são revogados em massa e a chamada falha.</p>
     *
     * @param rawToken valor raw recebido do cliente
     * @throws RegraNegocioException (FII0024) se o token não existir, estiver
     *         expirado, já tiver sido revogado, ou em caso de reuse detection
     */
    @Transactional
    public Rotacionado rotacionar(final String rawToken) {
        RefreshToken atual = refreshTokenRepository.findByTokenHash(hash(rawToken))
            .orElseThrow(() -> new RegraNegocioException(MensagemEnum.REFRESH_TOKEN_INVALIDO));

        if (atual.getRevokedAt() != null) {
            // Token revogado por logout — falha sem escalar.
            throw new RegraNegocioException(MensagemEnum.REFRESH_TOKEN_INVALIDO);
        }

        if (atual.getUsedAt() != null) {
            // Reuse detection: token já consumido foi tentado novamente.
            // Possível roubo — invalidamos todos os refreshes do usuário em
            // transação SEPARADA (REQUIRES_NEW), pois o RegraNegocioException
            // a seguir desfaria a revogação se ela rodasse na mesma tx.
            Long usuarioIdAlvo = atual.getUsuario().getId();
            revokeTxTemplate.executeWithoutResult(status -> {
                int afetados = refreshTokenRepository.revogarTodosDoUsuario(
                    usuarioIdAlvo, LocalDateTime.now());
                log.warn("Reuse detection para refresh token: usuário {} teve {} refresh(es) revogados",
                    usuarioIdAlvo, afetados);
            });
            throw new RegraNegocioException(MensagemEnum.REFRESH_TOKEN_INVALIDO);
        }

        if (atual.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException(MensagemEnum.REFRESH_TOKEN_INVALIDO);
        }

        // Rotação: emite novo, marca atual como usado apontando para o novo.
        Emitido novo = emitir(atual.getUsuario());
        atual.marcarComoUsado(novo.entidade());
        refreshTokenRepository.save(atual);

        return new Rotacionado(atual.getUsuario(), novo.rawToken());
    }

    /**
     * Revoga um refresh token específico (logout). Idempotente: se o token
     * não existe ou já está revogado/usado, a chamada termina silenciosamente
     * para não vazar informação sobre validade.
     */
    @Transactional
    public void revogar(final String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
            .filter(t -> t.getRevokedAt() == null && t.getUsedAt() == null)
            .ifPresent(RefreshToken::revogar);
    }

    private String gerarRawToken() {
        byte[] bytes = new byte[RANDOM_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(final String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 é mandatório em qualquer JRE — esse catch é defensivo apenas.
            throw new IllegalStateException("Algoritmo " + HASH_ALGORITHM + " não disponível", ex);
        }
    }

    /**
     * Resultado de {@link #emitir(Usuario)}: a entidade persistida e o raw a
     * enviar ao cliente (não persistido).
     */
    public record Emitido(RefreshToken entidade, String rawToken) {
    }

    /**
     * Resultado de {@link #rotacionar(String)}: o usuário dono do refresh
     * (para o caller gerar novo access JWT) e o novo refresh raw para devolver
     * ao cliente.
     */
    public record Rotacionado(Usuario usuario, String novoRefreshRaw) {
    }
}
