package com.renlip.fiis.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

import com.renlip.fiis.controller.AbstractControllerTests;
import com.renlip.fiis.domain.dto.OperacaoResponse;
import com.renlip.fiis.domain.enumeration.TipoOperacao;
import com.renlip.fiis.domain.event.OperacaoAtualizadaEvent;
import com.renlip.fiis.domain.event.OperacaoCriadaEvent;
import com.renlip.fiis.domain.event.OperacaoExcluidaEvent;
import com.renlip.fiis.domain.vo.OperacaoRequest;
import com.renlip.fiis.exception.RegraNegocioException;
import com.renlip.fiis.service.OperacaoService;

/**
 * Testes de integração que validam a publicação de eventos de domínio pela
 * camada de Operacao.
 *
 * <p>Usa {@link RecordApplicationEvents} para capturar todos os eventos
 * disparados durante o teste — independente de qualquer listener. Valida o
 * <b>contrato do publisher</b> (quais eventos saem e com que conteúdo);
 * confiamos que o Spring entrega aos {@code @TransactionalEventListener}
 * corretamente nas fases de commit/rollback.</p>
 *
 * <p>Os testes invocam o service diretamente (sem HTTP) — autenticação ainda
 * vem do {@code @WithUserDetails("test@fiis.com")} herdado de
 * {@link AbstractControllerTests}, necessária para o
 * {@code UsuarioLogadoSupport} resolver o usuário requisitante.</p>
 */
@RecordApplicationEvents
@SqlGroup({
    @Sql(value = "/fixtures/setup.sql",               executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/fixtures/cotacoes/fii-script.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
})
class AuditoriaEventosIntegrationTests extends AbstractControllerTests {

    @Autowired
    private OperacaoService operacaoService;

    @Autowired
    private ApplicationEvents events;

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

    @Nested
    @DisplayName("OperacaoCriadaEvent")
    class Criar {

        @Test
        @DisplayName("Publica 1 evento com snapshot dos campos relevantes após criar")
        void testCriarPublicaEvento() {
            OperacaoResponse response = operacaoService.criar(compraValida(1L));

            List<OperacaoCriadaEvent> publicados =
                events.stream(OperacaoCriadaEvent.class).toList();

            assertThat(publicados).hasSize(1);
            OperacaoCriadaEvent event = publicados.get(0);
            assertThat(event.operacaoId()).isEqualTo(response.id());
            assertThat(event.fundoId()).isEqualTo(1L);
            assertThat(event.ticker()).isEqualTo("HGLG11");
            assertThat(event.tipo()).isEqualTo(TipoOperacao.COMPRA);
            assertThat(event.quantidade()).isEqualTo(10);
            assertThat(event.precoUnitario()).isEqualByComparingTo("158.50");
            assertThat(event.dataOperacao()).isEqualTo(LocalDate.of(2026, 4, 17));
            assertThat(event.usuarioDonoId()).isEqualTo(1L);
            assertThat(event.usuarioRequisitanteId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("VENDA inválida (sem cotas em carteira) NÃO publica evento")
        void testVendaInvalidaNaoPublica() {
            // Fundo 1 não tem operações de COMPRA prévias — posição atual = 0.
            // A validação dispara antes do save, garantindo que o publish nem
            // chega a ser executado.
            OperacaoRequest venda = new OperacaoRequest(
                1L,
                TipoOperacao.VENDA,
                LocalDate.of(2026, 4, 17),
                10,
                new BigDecimal("160.00"),
                BigDecimal.ZERO,
                null);

            assertThatThrownBy(() -> operacaoService.criar(venda))
                .isInstanceOf(RegraNegocioException.class);

            assertThat(events.stream(OperacaoCriadaEvent.class).count()).isZero();
        }
    }

    @Nested
    @DisplayName("OperacaoAtualizadaEvent")
    class Atualizar {

        @Test
        @DisplayName("Publica 1 evento com snapshot pós-edição")
        void testAtualizarPublicaEvento() {
            OperacaoResponse criada = operacaoService.criar(compraValida(1L));

            OperacaoRequest edicao = new OperacaoRequest(
                1L,
                TipoOperacao.COMPRA,
                LocalDate.of(2026, 4, 18),
                15,
                new BigDecimal("160.00"),
                BigDecimal.ZERO,
                "Aumento de posição");
            operacaoService.atualizar(criada.id(), edicao);

            List<OperacaoAtualizadaEvent> publicados =
                events.stream(OperacaoAtualizadaEvent.class).toList();

            assertThat(publicados).hasSize(1);
            OperacaoAtualizadaEvent event = publicados.get(0);
            assertThat(event.operacaoId()).isEqualTo(criada.id());
            assertThat(event.quantidade()).isEqualTo(15);
            assertThat(event.precoUnitario()).isEqualByComparingTo("160.00");
            assertThat(event.dataOperacao()).isEqualTo(LocalDate.of(2026, 4, 18));
            assertThat(event.usuarioRequisitanteId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("OperacaoExcluidaEvent")
    class Excluir {

        @Test
        @DisplayName("Publica 1 evento carregando o ticker e o dono no momento da exclusão")
        void testExcluirPublicaEvento() {
            OperacaoResponse criada = operacaoService.criar(compraValida(1L));

            operacaoService.deletar(criada.id());

            List<OperacaoExcluidaEvent> publicados =
                events.stream(OperacaoExcluidaEvent.class).toList();

            assertThat(publicados).hasSize(1);
            OperacaoExcluidaEvent event = publicados.get(0);
            assertThat(event.operacaoId()).isEqualTo(criada.id());
            assertThat(event.fundoId()).isEqualTo(1L);
            assertThat(event.ticker()).isEqualTo("HGLG11");
            assertThat(event.usuarioDonoId()).isEqualTo(1L);
            assertThat(event.usuarioRequisitanteId()).isEqualTo(1L);
        }
    }
}
