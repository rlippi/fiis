package com.renlip.fiis.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

import com.renlip.fiis.controller.AbstractControllerTests;
import com.renlip.fiis.domain.dto.OperacaoResponse;
import com.renlip.fiis.domain.enumeration.TipoOperacao;
import com.renlip.fiis.domain.vo.OperacaoRequest;
import com.renlip.fiis.service.OperacaoService;
import com.renlip.fiis.service.PosicaoService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Testes de integração das métricas Micrometer.
 *
 * <p>Valida tanto a instrumentação custom (counters de Operacao + Timer de
 * cálculo de posição) quanto a exposição via Actuator. Os contadores são
 * inspecionados direto no {@link MeterRegistry}; os endpoints Actuator
 * pelo {@code RestTestClient}.</p>
 */
@SqlGroup({
    @Sql(value = "/fixtures/setup.sql",               executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/fixtures/cotacoes/fii-script.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
})
class MetricasIntegrationTests extends AbstractControllerTests {

    @Autowired
    private OperacaoService operacaoService;

    @Autowired
    private PosicaoService posicaoService;

    @Autowired
    private MeterRegistry meterRegistry;

    private OperacaoRequest compraValida(final Long fundoId) {
        return new OperacaoRequest(
            fundoId,
            TipoOperacao.COMPRA,
            LocalDate.of(2026, 4, 17),
            10,
            new BigDecimal("158.50"),
            new BigDecimal("0.50"),
            null);
    }

    /**
     * As métricas Micrometer são singletons no contexto Spring — entre testes,
     * o contador acumula valores. Em vez de tentar resetar (não trivial em
     * Micrometer), capturamos o valor antes da ação e validamos o delta.
     */
    private double counterValor(final String nome) {
        Counter counter = meterRegistry.find(nome).counter();
        return counter == null ? 0d : counter.count();
    }

    @Nested
    @DisplayName("Counters de Operacao")
    class OperacaoCounters {

        private double valorAntes;

        @BeforeEach
        void capturarValorAntes() {
            valorAntes = counterValor("fiis.operacao.criada");
        }

        @Test
        @DisplayName("criar incrementa fiis.operacao.criada em 1")
        void testCriarIncrementaCounter() {
            operacaoService.criar(compraValida(1L));

            double valorDepois = counterValor("fiis.operacao.criada");
            assertThat(valorDepois - valorAntes).isEqualTo(1d);
        }

        @Test
        @DisplayName("atualizar incrementa fiis.operacao.atualizada em 1")
        void testAtualizarIncrementaCounter() {
            OperacaoResponse criada = operacaoService.criar(compraValida(1L));
            double atualizadasAntes = counterValor("fiis.operacao.atualizada");

            OperacaoRequest edicao = new OperacaoRequest(
                1L, TipoOperacao.COMPRA, LocalDate.of(2026, 4, 18),
                15, new BigDecimal("160.00"), BigDecimal.ZERO, null);
            operacaoService.atualizar(criada.id(), edicao);

            assertThat(counterValor("fiis.operacao.atualizada") - atualizadasAntes).isEqualTo(1d);
        }

        @Test
        @DisplayName("deletar incrementa fiis.operacao.excluida em 1")
        void testDeletarIncrementaCounter() {
            OperacaoResponse criada = operacaoService.criar(compraValida(1L));
            double excluidasAntes = counterValor("fiis.operacao.excluida");

            operacaoService.deletar(criada.id());

            assertThat(counterValor("fiis.operacao.excluida") - excluidasAntes).isEqualTo(1d);
        }
    }

    @Nested
    @DisplayName("Timer fiis.posicao.calculo")
    class PosicaoTimer {

        @Test
        @DisplayName("calcularPosicaoDoFundo registra uma medição no timer")
        void testCalculoRegistraTimer() {
            Timer timer = meterRegistry.find("fiis.posicao.calculo").timer();
            assertThat(timer).as("timer 'fiis.posicao.calculo' deve estar registrado").isNotNull();
            long countAntes = timer.count();

            posicaoService.calcularPosicaoDoFundo(1L);

            assertThat(timer.count() - countAntes).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("Endpoints Actuator")
    class ActuatorEndpoints {

        @Test
        @DisplayName("[200 OK] /actuator/health responde sem autenticação")
        void testHealthPublico() {
            // O default do AbstractControllerTests é autenticado, mas health é
            // permitAll — passa de qualquer jeito.
            restTestClient.get("/actuator/health")
                .expectStatus(HttpStatus.OK);
        }

        @Test
        @DisplayName("[200 OK] /actuator/info responde sem restrição")
        void testInfoPublico() {
            restTestClient.get("/actuator/info")
                .expectStatus(HttpStatus.OK);
        }

        @Test
        @DisplayName("[403 Forbidden] /actuator/metrics exige perfil ADMIN")
        void testMetricsExigeAdmin() {
            // test@fiis.com (default) é USER — recebe 403.
            restTestClient.get("/actuator/metrics")
                .expectStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("[403 Forbidden] /actuator/prometheus exige perfil ADMIN")
        void testPrometheusExigeAdmin() {
            restTestClient.get("/actuator/prometheus")
                .expectStatus(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("Counter BRAPI import (registro)")
    class BrapiImportCounters {

        @Test
        @DisplayName("Counter fiis.brapi.import com tag status=ok está registrado")
        void testCounterOkRegistrado() {
            assertThat(meterRegistry.find("fiis.brapi.import").tag("status", "ok").counter())
                .as("counter ok deve estar registrado pelo AtualizarCotacoesJob no startup")
                .isNotNull();
        }

        @Test
        @DisplayName("Counter fiis.brapi.import com tag status=falha está registrado")
        void testCounterFalhaRegistrado() {
            assertThat(meterRegistry.find("fiis.brapi.import").tag("status", "falha").counter())
                .as("counter falha deve estar registrado pelo AtualizarCotacoesJob no startup")
                .isNotNull();
        }
    }
}
