package com.renlip.fiis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Configurações dos caches da aplicação.
 *
 * <p>Ligadas ao prefixo {@code fiis.cache} em {@code application.yml}. Cada
 * sub-record representa um cache nomeado, permitindo ajustar TTL e tamanho
 * máximo por env var sem redeploy.</p>
 */
@ConfigurationProperties(prefix = "fiis.cache")
public record CacheProperties(

    @NotNull UltimaCotacao ultimaCotacao
) {

    /**
     * Configuração do cache da "última cotação por fundo".
     *
     * @param ttlMinutos tempo após o qual uma entrada cacheada expira e é
     *                   recarregada do banco. TTL curto serve de rede de
     *                   segurança caso alguma escrita escape do
     *                   {@code @CacheEvict} explícito.
     * @param maxSize    número máximo de entradas. Cada usuário ativo tem
     *                   ~10 fundos; 500 acomoda dezenas de carteiras
     *                   simultâneas com folga.
     */
    public record UltimaCotacao(
        @Min(1) long ttlMinutos,
        @Min(1) long maxSize
    ) {
    }
}
