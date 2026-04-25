package com.renlip.fiis.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Habilita a abstração de cache do Spring e configura o {@link CaffeineCacheManager}.
 *
 * <p>O bean {@link CacheManager} explícito sobrescreve o auto-config do Spring
 * Boot — necessário porque queremos controlar TTL ({@code expireAfterWrite}) e
 * tamanho máximo ({@code maximumSize}) por cache, comportamentos que o
 * default ({@code ConcurrentMapCacheManager}) não oferece.</p>
 *
 * <p>Os nomes dos caches são expostos como constantes para evitar duplicação
 * de strings nas anotações {@code @Cacheable}/{@code @CacheEvict}.</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Nome do cache que armazena a última cotação por {@code fundoId}.
     * Usado em {@code @Cacheable} no {@code CotacaoService} e em
     * {@code @CacheEvict} nos métodos que alteram cotações.
     */
    public static final String CACHE_ULTIMA_COTACAO_POR_FUNDO = "ultimaCotacaoPorFundoCache";

    /**
     * {@link CacheManager} baseado em Caffeine com configuração específica para
     * cada cache nomeado. Tamanho e TTL são lidos de {@link CacheProperties} —
     * variáveis de ambiente {@code FIIS_CACHE_ULTIMA_COTACAO_*} sobrescrevem
     * sem redeploy.
     */
    @Bean
    public CacheManager cacheManager(final CacheProperties properties) {
        CaffeineCacheManager manager = new CaffeineCacheManager(CACHE_ULTIMA_COTACAO_POR_FUNDO);
        manager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(properties.ultimaCotacao().ttlMinutos()))
            .maximumSize(properties.ultimaCotacao().maxSize()));
        return manager;
    }
}
