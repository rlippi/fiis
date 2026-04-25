package com.renlip.fiis.service;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.config.CacheConfig;
import com.renlip.fiis.domain.dto.UltimaCotacaoResumo;
import com.renlip.fiis.repository.CotacaoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service especializado em fornecer cotações via cache.
 *
 * <p><b>Por que é uma classe à parte do {@code CotacaoService}:</b> {@code @Cacheable}
 * só funciona quando o método é invocado <i>através do proxy</i> do Spring.
 * Self-invocation dentro da mesma classe pula o proxy. Mantendo o método cacheado
 * em um service separado, qualquer chamador (incluindo {@link PosicaoService})
 * passa pelo proxy e o cache atua corretamente.</p>
 *
 * <p>O retorno é {@link UltimaCotacaoResumo} (não {@code Cotacao}) deliberadamente:
 * a entidade JPA tem associações lazy que se perderiam ao sair da sessão Hibernate
 * que originou o objeto. Um record imutável com apenas tipos primitivos é seguro
 * para servir do cache em qualquer thread/sessão.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CotacaoCacheService {

    private final CotacaoRepository cotacaoRepository;

    /**
     * Retorna a última cotação ({@code data} + {@code precoFechamento}) de um fundo,
     * com cache local de 30 minutos (configurável). Chave: {@code fundoId}.
     *
     * <p>{@code fundoId} é PK global única por usuário (em {@code fundo} a unique
     * key é {@code (usuario_id, ticker)}, e o {@code id} é serial), portanto não
     * há vazamento de dados entre usuários ao compartilhar este cache.</p>
     *
     * @param fundoId ID do fundo
     * @return resumo da última cotação, ou {@link Optional#empty()} se o fundo
     *         ainda não tem cotações
     */
    @Cacheable(value = CacheConfig.CACHE_ULTIMA_COTACAO_POR_FUNDO, key = "#fundoId")
    public Optional<UltimaCotacaoResumo> buscarUltimaCotacaoPorFundo(final Long fundoId) {
        return cotacaoRepository.findFirstByFundoIdOrderByDataDesc(fundoId)
            .map(c -> new UltimaCotacaoResumo(c.getData(), c.getPrecoFechamento()));
    }
}
