package com.renlip.fiis.audit;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.renlip.fiis.domain.event.OperacaoAtualizadaEvent;
import com.renlip.fiis.domain.event.OperacaoCriadaEvent;
import com.renlip.fiis.domain.event.OperacaoExcluidaEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Listener de auditoria que registra log estruturado das mudanças de domínio.
 *
 * <p><b>Por que {@link TransactionalEventListener} com {@code AFTER_COMMIT}:</b>
 * eventos publicados via {@code ApplicationEventPublisher} dentro de uma transação
 * só são entregues a este listener <i>depois</i> do commit. Se a transação fizer
 * rollback (validação de regra de negócio, falha de banco, etc.), o evento é
 * descartado e nada é auditado — auditamos apenas o que foi de fato persistido.</p>
 *
 * <p><b>Por que log estruturado e não tabela:</b> começamos com SLF4J por ter
 * baixo custo, integração imediata com qualquer agregador (Logtail, Grafana
 * Loki, ELK) e zero impacto em transações. Persistir em tabela {@code auditoria}
 * é uma extensão futura — a separação Listener/Domain Event garante que o
 * publisher não precisa mudar quando isso acontecer.</p>
 *
 * <p>O formato {@code chave=valor} (logfmt) é fácil de parsear por agregadores
 * sem precisar configurar um JSON encoder no Logback agora.</p>
 */
@Component
@Slf4j
public class AuditoriaListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOperacaoCriada(final OperacaoCriadaEvent event) {
        log.info("AUDIT acao=CRIAR recurso=Operacao id={} fundoId={} ticker={} tipo={} qty={} preco={} data={} usuarioDono={} usuarioRequisitante={}",
            event.operacaoId(), event.fundoId(), event.ticker(),
            event.tipo(), event.quantidade(), event.precoUnitario(), event.dataOperacao(),
            event.usuarioDonoId(), event.usuarioRequisitanteId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOperacaoAtualizada(final OperacaoAtualizadaEvent event) {
        log.info("AUDIT acao=ATUALIZAR recurso=Operacao id={} fundoId={} ticker={} tipo={} qty={} preco={} data={} usuarioDono={} usuarioRequisitante={}",
            event.operacaoId(), event.fundoId(), event.ticker(),
            event.tipo(), event.quantidade(), event.precoUnitario(), event.dataOperacao(),
            event.usuarioDonoId(), event.usuarioRequisitanteId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOperacaoExcluida(final OperacaoExcluidaEvent event) {
        log.info("AUDIT acao=EXCLUIR recurso=Operacao id={} fundoId={} ticker={} usuarioDono={} usuarioRequisitante={}",
            event.operacaoId(), event.fundoId(), event.ticker(),
            event.usuarioDonoId(), event.usuarioRequisitanteId());
    }
}
