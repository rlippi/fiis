package com.renlip.fiis.support;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.renlip.fiis.config.RateLimitProperties;
import com.renlip.fiis.domain.enumeration.MensagemEnum;
import com.renlip.fiis.exception.LimiteRequisicoesException;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;

/**
 * Helper que aplica rate limiting token-bucket nas operações sensíveis da API.
 *
 * <p>Mantém um bucket por combinação {@code (endpoint, chave)} em memória. A
 * chave é provida pelo chamador — pode ser e-mail (login/forgot-password) ou
 * IP do cliente (signup), por exemplo.</p>
 *
 * <p>Storage in-memory (ConcurrentHashMap). Na escala atual (1 instância Render
 * free) é suficiente. Se houver escala horizontal, trocar pela variante
 * distribuída do Bucket4j sem afetar o código dos services.</p>
 */
@Component
@RequiredArgsConstructor
public class RateLimitSupport {

    private final RateLimitProperties properties;

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Consome 1 token do bucket de login associado ao e-mail informado. Se o
     * bucket estiver vazio, lança {@link LimiteRequisicoesException} (HTTP 429).
     *
     * @param email identificador da conta sendo autenticada (case-insensitive)
     */
    public void consumirLogin(final String email) {
        consumir("login:" + normalizar(email), properties.login().capacidade(),
            properties.login().duracaoMinutos());
    }

    /**
     * Consome 1 token do bucket de signup associado ao IP do cliente.
     *
     * @param ip endereço IP (via {@code HttpServletRequest.getRemoteAddr()} ou X-Forwarded-For)
     */
    public void consumirSignup(final String ip) {
        consumir("signup:" + normalizar(ip), properties.signup().capacidade(),
            properties.signup().duracaoMinutos());
    }

    private void consumir(final String chave, final int capacidade, final long duracaoMinutos) {
        Bucket bucket = buckets.computeIfAbsent(chave, k -> novoBucket(capacidade, duracaoMinutos));
        if (!bucket.tryConsume(1)) {
            throw new LimiteRequisicoesException(MensagemEnum.LIMITE_REQUISICOES_EXCEDIDO);
        }
    }

    /**
     * Reinicia todos os buckets (para uso em testes de integração que fazem
     * várias chamadas aos endpoints com rate limit). Não use em produção.
     */
    public void limpar() {
        buckets.clear();
    }

    private Bucket novoBucket(final int capacidade, final long duracaoMinutos) {
        Bandwidth limite = Bandwidth.builder()
            .capacity(capacidade)
            .refillGreedy(capacidade, Duration.ofMinutes(duracaoMinutos))
            .build();
        return Bucket.builder().addLimit(limite).build();
    }

    private String normalizar(final String chave) {
        return chave == null ? "" : chave.trim().toLowerCase();
    }
}
