package com.renlip.fiis.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.domain.dto.PosicaoResponse;
import com.renlip.fiis.domain.entity.Cotacao;
import com.renlip.fiis.domain.entity.EventoCorporativo;
import com.renlip.fiis.domain.entity.Fundo;
import com.renlip.fiis.domain.entity.Operacao;
import com.renlip.fiis.domain.entity.Provento;
import com.renlip.fiis.domain.enumeration.MensagemEnum;
import com.renlip.fiis.domain.enumeration.TipoEventoCorporativo;
import com.renlip.fiis.domain.enumeration.TipoOperacao;
import com.renlip.fiis.domain.mapper.FundoResumoMapper;
import com.renlip.fiis.repository.CotacaoRepository;
import com.renlip.fiis.repository.EventoCorporativoRepository;
import com.renlip.fiis.repository.FundoRepository;
import com.renlip.fiis.repository.OperacaoRepository;
import com.renlip.fiis.repository.ProventoRepository;
import com.renlip.fiis.exception.RecursoNaoEncontradoException;

import static com.renlip.fiis.constant.EscalaConstants.ESCALA_CALCULO;
import static com.renlip.fiis.constant.EscalaConstants.ESCALA_MONETARIA;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável pelo cálculo da <b>posição consolidada</b> por fundo.
 *
 * <p>Combina dados de {@link Operacao}, {@link Provento}, {@link Cotacao} e
 * {@link EventoCorporativo} para produzir um resumo com quantidade, preço
 * médio, custo, lucro realizado, proventos recebidos, yield, valor atual
 * de mercado e rentabilidade total.</p>
 *
 * <p><b>Regras do preço médio (padrão do mercado brasileiro):</b>
 * <ul>
 *   <li><b>COMPRA:</b> PM = (custo_anterior + valor_compra) / (qtd_anterior + qtd_compra);</li>
 *   <li><b>VENDA:</b> PM não muda, custo é reduzido proporcionalmente (qtd_vendida × PM);</li>
 *   <li><b>BONIFICACAO:</b> qty × (1 + fator), custo preservado, PM diminui;</li>
 *   <li><b>DESDOBRAMENTO:</b> qty × fator, custo preservado, PM diminui;</li>
 *   <li><b>GRUPAMENTO:</b> qty / fator, custo preservado, PM aumenta.</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PosicaoService {

    private final FundoRepository fundoRepository;
    private final OperacaoRepository operacaoRepository;
    private final ProventoRepository proventoRepository;
    private final CotacaoRepository cotacaoRepository;
    private final EventoCorporativoRepository eventoRepository;
    private final FundoResumoMapper fundoResumoMapper;

    /**
     * Calcula a posição consolidada de todos os fundos ativos na carteira.
     *
     * @return lista de posições (uma por fundo)
     */
    public List<PosicaoResponse> calcularPosicaoDeTodos() {
        return fundoRepository.findByAtivoTrue().stream()
            .map(this::calcularPosicao)
            .toList();
    }

    /**
     * Calcula a posição consolidada de um fundo específico.
     *
     * @param fundoId ID do fundo
     * @return posição consolidada
     * @throws RecursoNaoEncontradoException se o fundo não existir
     */
    public PosicaoResponse calcularPosicaoDoFundo(Long fundoId) {
        Fundo fundo = fundoRepository.findById(fundoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException(MensagemEnum.FUNDO_NAO_ENCONTRADO, fundoId));
        return calcularPosicao(fundo);
    }

    /**
     * Estado mutável da posição durante o processamento cronológico.
     * Evita retornar múltiplos valores por método.
     */
    private static final class EstadoPosicao {
        BigDecimal qty = BigDecimal.ZERO;
        BigDecimal custo = BigDecimal.ZERO;
        BigDecimal pm = BigDecimal.ZERO;
        BigDecimal totalCompras = BigDecimal.ZERO;
        BigDecimal totalVendas = BigDecimal.ZERO;
        BigDecimal lucroRealizado = BigDecimal.ZERO;
    }

    /**
     * Item genérico da linha do tempo (operação ou evento corporativo),
     * usado para processar os dois em ordem cronológica única.
     */
    private record ItemTimeline(LocalDate data, Operacao operacao, EventoCorporativo evento) {
        static ItemTimeline de(Operacao op) {
            return new ItemTimeline(op.getDataOperacao(), op, null);
        }
        static ItemTimeline de(EventoCorporativo ev) {
            return new ItemTimeline(ev.getData(), null, ev);
        }
    }

    /**
     * Executa o cálculo completo da posição de um fundo.
     *
     * <p>Processa operações e eventos corporativos na ordem cronológica em
     * que aconteceram, mantendo os indicadores consolidados.</p>
     */
    private PosicaoResponse calcularPosicao(Fundo fundo) {
        List<Operacao> operacoes = operacaoRepository.findByFundoIdOrderByDataOperacaoDesc(fundo.getId());
        List<EventoCorporativo> eventos = eventoRepository.findByFundoIdOrderByDataAsc(fundo.getId());

        List<ItemTimeline> timeline = Stream.concat(
            operacoes.stream().map(ItemTimeline::de),
            eventos.stream().map(ItemTimeline::de)
        ).sorted(Comparator.comparing(ItemTimeline::data)).toList();

        EstadoPosicao estado = new EstadoPosicao();

        for (ItemTimeline item : timeline) {
            if (item.operacao() != null) {
                aplicarOperacao(item.operacao(), estado);
            } else {
                aplicarEvento(item.evento(), estado);
            }
        }

        List<Provento> proventos = proventoRepository.findByFundoIdOrderByDataReferenciaDesc(fundo.getId());
        BigDecimal totalProventos = proventos.stream()
            .map(Provento::calcularValorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal custoAtual = estado.qty.multiply(estado.pm).setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
        BigDecimal yieldSobreCusto = custoAtual.signum() > 0
            ? totalProventos.multiply(BigDecimal.valueOf(100))
                .divide(custoAtual, ESCALA_MONETARIA, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        Optional<Cotacao> ultimaCotacao = cotacaoRepository.findFirstByFundoIdOrderByDataDesc(fundo.getId());
        BigDecimal precoAtual = ultimaCotacao.map(Cotacao::getPrecoFechamento).orElse(null);
        LocalDate dataUltimaCotacao = ultimaCotacao.map(Cotacao::getData).orElse(null);

        BigDecimal valorAtual = precoAtual != null
            ? estado.qty.multiply(precoAtual).setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal variacao = (precoAtual != null && estado.pm.signum() > 0)
            ? precoAtual.subtract(estado.pm).multiply(BigDecimal.valueOf(100))
                .divide(estado.pm, ESCALA_MONETARIA, RoundingMode.HALF_UP)
            : null;

        BigDecimal rentabilidadeTotal = calcularRentabilidadeTotal(
            estado.totalCompras, valorAtual, estado.totalVendas, totalProventos);

        return new PosicaoResponse(
            fundoResumoMapper.toResponse(fundo),
            estado.qty.intValue(),
            estado.pm.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            custoAtual,
            estado.totalCompras.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            estado.totalVendas.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            estado.lucroRealizado.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            totalProventos.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            yieldSobreCusto,
            precoAtual,
            dataUltimaCotacao,
            valorAtual,
            variacao,
            rentabilidadeTotal,
            operacoes.size(),
            proventos.size()
        );
    }

    /**
     * Aplica o efeito de uma operação (COMPRA ou VENDA) no estado da posição.
     */
    private void aplicarOperacao(Operacao op, EstadoPosicao e) {
        BigDecimal opQty = BigDecimal.valueOf(op.getQuantidade());
        BigDecimal opValor = op.calcularValorTotal();

        if (op.getTipo() == TipoOperacao.COMPRA) {
            e.totalCompras = e.totalCompras.add(opValor);
            e.custo = e.custo.add(opValor);
            e.qty = e.qty.add(opQty);
            e.pm = e.qty.signum() > 0
                ? e.custo.divide(e.qty, ESCALA_CALCULO, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        } else {
            e.totalVendas = e.totalVendas.add(opValor);
            BigDecimal custoDasCotasVendidas = e.pm.multiply(opQty);
            e.lucroRealizado = e.lucroRealizado.add(opValor.subtract(custoDasCotasVendidas));
            e.custo = e.custo.subtract(custoDasCotasVendidas);
            e.qty = e.qty.subtract(opQty);
            if (e.qty.signum() <= 0) {
                e.qty = BigDecimal.ZERO;
                e.custo = BigDecimal.ZERO;
                e.pm = BigDecimal.ZERO;
            }
        }
    }

    /**
     * Aplica o efeito de um evento corporativo no estado da posição.
     *
     * <p><b>Comportamento:</b>
     * <ul>
     *   <li>Calcula a quantidade teórica pós-evento (pode ser fracional);</li>
     *   <li>Calcula o novo PM proporcional (preserva a relação custo/quantidade);</li>
     *   <li><b>Trunca a quantidade para inteiro</b> — frações são pagas em
     *       dinheiro pela corretora e não ficam em carteira;</li>
     *   <li>Ajusta o custo como {@code qty × PM} (custo reduz junto com as frações).</li>
     * </ul>
     * </p>
     *
     * <p>Se no momento do evento a posição estiver zerada, o evento é ignorado.</p>
     */
    private void aplicarEvento(EventoCorporativo evento, EstadoPosicao e) {
        if (e.qty.signum() <= 0) {
            return;
        }

        BigDecimal fator = evento.getFator();
        TipoEventoCorporativo tipo = evento.getTipo();

        BigDecimal novaQtyFracional = switch (tipo) {
            case BONIFICACAO -> e.qty.multiply(BigDecimal.ONE.add(fator));
            case DESDOBRAMENTO -> e.qty.multiply(fator);
            case GRUPAMENTO -> e.qty.divide(fator, ESCALA_CALCULO, RoundingMode.HALF_UP);
        };

        BigDecimal pmNovo = novaQtyFracional.signum() > 0
            ? e.custo.divide(novaQtyFracional, ESCALA_CALCULO, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal novaQtyInteira = novaQtyFracional.setScale(0, RoundingMode.DOWN);

        e.qty = novaQtyInteira;
        e.pm = pmNovo;
        e.custo = novaQtyInteira.multiply(pmNovo);
    }

    /**
     * Rentabilidade total sobre o capital investido no fundo.
     *
     * <p>Fórmula: {@code (valorAtual + totalVendas + totalProventos − totalCompras) / totalCompras × 100}.</p>
     */
    private BigDecimal calcularRentabilidadeTotal(BigDecimal totalCompras, BigDecimal valorAtual,
                                                  BigDecimal totalVendas, BigDecimal totalProventos) {
        if (totalCompras.signum() <= 0) {
            return null;
        }
        BigDecimal retornoTotal = valorAtual
            .add(totalVendas)
            .add(totalProventos)
            .subtract(totalCompras);
        return retornoTotal.multiply(BigDecimal.valueOf(100))
            .divide(totalCompras, ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }
}
