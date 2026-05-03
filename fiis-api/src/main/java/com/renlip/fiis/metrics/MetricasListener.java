package com.renlip.fiis.metrics;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.renlip.fiis.domain.event.OperacaoAtualizadaEvent;
import com.renlip.fiis.domain.event.OperacaoCriadaEvent;
import com.renlip.fiis.domain.event.OperacaoExcluidaEvent;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Listener que incrementa contadores Micrometer a cada evento de domínio de
 * {@code Operacao}.
 *
 * <p><b>Por que classe separada do {@code AuditoriaListener}:</b> métricas e
 * auditoria são preocupações distintas — a primeira existe para observabilidade
 * em tempo quase-real (Prometheus/Grafana), a segunda para rastreabilidade
 * histórica em log estruturado. Mantendo um listener por concern, cada um pode
 * evoluir independente (mudar formato de log não afeta métricas, etc.).</p>
 *
 * <p><b>Por que {@link EventListener} e não {@code @TransactionalEventListener}:</b>
 * para métricas operacionais, contar tentativas (incluindo as que falharam) é
 * tão útil quanto contar sucessos — taxa de erro vira métrica em si. Se
 * fôssemos usar {@code AFTER_COMMIT}, perderíamos visibilidade de
 * rollbacks. O {@code AuditoriaListener} usa {@code AFTER_COMMIT} pelo motivo
 * oposto: auditoria só registra fatos consumados.</p>
 *
 * <p>Os contadores são criados <i>lazy</i> no construtor via {@link Counter#builder}
 * para evitar lookups repetidos a cada evento.</p>
 */
@Component
public class MetricasListener {

    private final Counter operacaoCriadaCounter;

    private final Counter operacaoAtualizadaCounter;

    private final Counter operacaoExcluidaCounter;

    public MetricasListener(final MeterRegistry meterRegistry) {
        this.operacaoCriadaCounter = Counter.builder("fiis.operacao.criada")
            .description("Total de operações criadas (compra ou venda)")
            .register(meterRegistry);
        this.operacaoAtualizadaCounter = Counter.builder("fiis.operacao.atualizada")
            .description("Total de operações editadas")
            .register(meterRegistry);
        this.operacaoExcluidaCounter = Counter.builder("fiis.operacao.excluida")
            .description("Total de operações removidas")
            .register(meterRegistry);
    }

    @EventListener
    public void onOperacaoCriada(final OperacaoCriadaEvent event) {
        operacaoCriadaCounter.increment();
    }

    @EventListener
    public void onOperacaoAtualizada(final OperacaoAtualizadaEvent event) {
        operacaoAtualizadaCounter.increment();
    }

    @EventListener
    public void onOperacaoExcluida(final OperacaoExcluidaEvent event) {
        operacaoExcluidaCounter.increment();
    }
}
