package com.renlip.fiis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

import com.renlip.fiis.config.CacheConfig;
import com.renlip.fiis.controller.AbstractControllerTests;
import com.renlip.fiis.domain.dto.UltimaCotacaoResumo;
import com.renlip.fiis.domain.dto.brapi.BrapiQuote;
import com.renlip.fiis.domain.dto.brapi.BrapiQuoteResponse;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.vo.CotacaoRequest;
import com.renlip.fiis.repository.UsuarioRepository;
import com.renlip.fiis.support.BrapiClient;

/**
 * Testes de integração do cache da última cotação por fundo.
 *
 * <p>Valida três comportamentos chave:
 * <ul>
 *   <li>Hit/miss: a primeira chamada popula o cache; a segunda devolve do cache;</li>
 *   <li>Eviction cirúrgica: {@code criar()} invalida apenas a entrada do
 *       {@code fundoId} envolvido;</li>
 *   <li>Eviction global: {@code importarViaBrapiPara()} ({@code allEntries=true})
 *       invalida o cache inteiro de uma vez.</li>
 * </ul>
 *
 * <p>Fixture: {@code /fixtures/cotacoes/fii-script.sql} cria 3 fundos
 * (HGLG11, MXRF11, VISC11) para o usuário de teste — IDs 1, 2 e 3.</p>
 */
@SqlGroup({
    @Sql(value = "/fixtures/setup.sql",               executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/fixtures/cotacoes/fii-script.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
})
class CotacaoCacheServiceTests extends AbstractControllerTests {

    @Autowired
    private CotacaoCacheService cotacaoCacheService;

    @Autowired
    private CotacaoService cotacaoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private BrapiClient brapiClient;

    private Cache ultimaCotacaoCache() {
        Cache cache = cacheManager.getCache(CacheConfig.CACHE_ULTIMA_COTACAO_POR_FUNDO);
        assertThat(cache).as("cache 'ultimaCotacaoPorFundoCache' deve estar configurado").isNotNull();
        return cache;
    }

    @Nested
    @DisplayName("Hit/miss básico")
    class HitMiss {

        @Test
        @DisplayName("Primeira chamada popula o cache; chave fundoId presente após retorno")
        void testPopulaCacheNaPrimeiraChamada() {
            // Antes da chamada, cache vazio (limpo no @BeforeEach do AbstractControllerTests).
            assertThat(ultimaCotacaoCache().get(1L)).isNull();

            Optional<UltimaCotacaoResumo> resumo = cotacaoCacheService.buscarUltimaCotacaoPorFundo(1L);

            assertThat(resumo).isPresent();
            assertThat(resumo.get().precoFechamento()).isEqualByComparingTo("158.5000");
            // Após a chamada, a entrada está no cache.
            assertThat(ultimaCotacaoCache().get(1L)).isNotNull();
        }

        @Test
        @DisplayName("Fundo sem cotações também é cacheado (Optional.empty)")
        void testFundoSemCotacoesCacheia() {
            // VISC11 (ID 3) é cadastrado sem cotações na fixture.
            Optional<UltimaCotacaoResumo> resumo = cotacaoCacheService.buscarUltimaCotacaoPorFundo(3L);

            assertThat(resumo).isEmpty();
            // Spring Cache cacheia o Optional.empty() também — evita martelar o
            // banco quando o fundo legitimamente não tem cotações.
            assertThat(ultimaCotacaoCache().get(3L)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Eviction")
    class Eviction {

        @Test
        @DisplayName("criar(): invalida a entrada do fundo afetado, preservando os demais")
        void testCriarEvictaApenasFundoAfetado() {
            cotacaoCacheService.buscarUltimaCotacaoPorFundo(1L);
            cotacaoCacheService.buscarUltimaCotacaoPorFundo(2L);
            assertThat(ultimaCotacaoCache().get(1L)).isNotNull();
            assertThat(ultimaCotacaoCache().get(2L)).isNotNull();

            CotacaoRequest novaCotacao = new CotacaoRequest(
                1L, LocalDate.of(2026, 4, 18), new BigDecimal("159.00"),
                null, null, null, null);
            cotacaoService.criar(novaCotacao);

            // key = "#request.fundoId()" → só fundo 1 sai do cache.
            assertThat(ultimaCotacaoCache().get(1L)).isNull();
            assertThat(ultimaCotacaoCache().get(2L)).isNotNull();
        }

        @Test
        @DisplayName("importarViaBrapiPara(): allEntries=true limpa o cache inteiro")
        void testImportarBrapiEvictaTodasAsEntradas() {
            cotacaoCacheService.buscarUltimaCotacaoPorFundo(1L);
            cotacaoCacheService.buscarUltimaCotacaoPorFundo(2L);
            cotacaoCacheService.buscarUltimaCotacaoPorFundo(3L);
            assertThat(ultimaCotacaoCache().get(1L)).isNotNull();
            assertThat(ultimaCotacaoCache().get(2L)).isNotNull();
            assertThat(ultimaCotacaoCache().get(3L)).isNotNull();

            // BRAPI devolve quote válida para cada ticker chamado.
            when(brapiClient.buscarCotacoes(any())).thenAnswer(invocation -> {
                List<String> tickers = invocation.getArgument(0);
                List<BrapiQuote> quotes = tickers.stream()
                    .map(t -> new BrapiQuote(t,
                        new BigDecimal("100.00"), null, null, null, null))
                    .toList();
                return new BrapiQuoteResponse(quotes);
            });

            Usuario usuario = usuarioRepository.findByEmail("test@fiis.com").orElseThrow();
            cotacaoService.importarViaBrapiPara(usuario);

            // allEntries = true → todas as entradas saem do cache.
            assertThat(ultimaCotacaoCache().get(1L)).isNull();
            assertThat(ultimaCotacaoCache().get(2L)).isNull();
            assertThat(ultimaCotacaoCache().get(3L)).isNull();
        }
    }
}
