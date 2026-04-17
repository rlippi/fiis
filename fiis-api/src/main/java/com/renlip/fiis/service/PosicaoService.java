package com.renlip.fiis.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.domain.enums.TipoOperacao;
import com.renlip.fiis.domain.model.Fundo;
import com.renlip.fiis.domain.model.Operacao;
import com.renlip.fiis.domain.model.Provento;
import com.renlip.fiis.domain.repository.FundoRepository;
import com.renlip.fiis.domain.repository.OperacaoRepository;
import com.renlip.fiis.domain.repository.ProventoRepository;
import com.renlip.fiis.dto.FundoResumoResponse;
import com.renlip.fiis.dto.PosicaoResponse;
import com.renlip.fiis.exception.RecursoNaoEncontradoException;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável pelo cálculo da <b>posição consolidada</b> por fundo.
 *
 * <p>Combina dados de {@link Operacao} e {@link Provento} para produzir
 * um resumo com quantidade, preço médio, custo, lucro realizado, proventos
 * recebidos e yield.</p>
 *
 * <p><b>Regra do preço médio (padrão do mercado brasileiro):</b>
 * <ul>
 *   <li>Em compras: PM = (custo_anterior + valor_compra) / (qtd_anterior + qtd_compra);</li>
 *   <li>Em vendas: PM <b>não muda</b>, custo é reduzido proporcionalmente (qtd_vendida × PM).</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PosicaoService {

    /** Escala utilizada em cálculos intermediários (preço médio). */
    private static final int ESCALA_CALCULO = 6;

    /** Escala utilizada nos valores monetários finais. */
    private static final int ESCALA_MONETARIA = 2;

    private final FundoRepository fundoRepository;
    private final OperacaoRepository operacaoRepository;
    private final ProventoRepository proventoRepository;

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
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Fundo com ID " + fundoId + " não encontrado"));
        return calcularPosicao(fundo);
    }

    /**
     * Executa o cálculo completo da posição de um fundo.
     *
     * <p>Percorre as operações em ordem cronológica mantendo
     * {@code qty}, {@code custoAcumulado} e {@code pm}. As vendas geram
     * lucro/prejuízo realizado.</p>
     */
    private PosicaoResponse calcularPosicao(Fundo fundo) {
        List<Operacao> operacoes = operacaoRepository.findByFundoIdOrderByDataOperacaoDesc(fundo.getId())
            .stream()
            .sorted(Comparator.comparing(Operacao::getDataOperacao))
            .toList();

        BigDecimal qty = BigDecimal.ZERO;
        BigDecimal custo = BigDecimal.ZERO;
        BigDecimal pm = BigDecimal.ZERO;
        BigDecimal totalCompras = BigDecimal.ZERO;
        BigDecimal totalVendas = BigDecimal.ZERO;
        BigDecimal lucroRealizado = BigDecimal.ZERO;

        for (Operacao op : operacoes) {
            BigDecimal opQty = BigDecimal.valueOf(op.getQuantidade());
            BigDecimal opValor = op.calcularValorTotal();

            if (op.getTipo() == TipoOperacao.COMPRA) {
                totalCompras = totalCompras.add(opValor);
                custo = custo.add(opValor);
                qty = qty.add(opQty);
                pm = qty.signum() > 0
                    ? custo.divide(qty, ESCALA_CALCULO, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            } else {
                totalVendas = totalVendas.add(opValor);
                BigDecimal custoDasCotasVendidas = pm.multiply(opQty);
                lucroRealizado = lucroRealizado.add(opValor.subtract(custoDasCotasVendidas));
                custo = custo.subtract(custoDasCotasVendidas);
                qty = qty.subtract(opQty);
                if (qty.signum() <= 0) {
                    qty = BigDecimal.ZERO;
                    custo = BigDecimal.ZERO;
                    pm = BigDecimal.ZERO;
                }
            }
        }

        List<Provento> proventos = proventoRepository.findByFundoIdOrderByDataReferenciaDesc(fundo.getId());
        BigDecimal totalProventos = proventos.stream()
            .map(Provento::calcularValorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal custoAtual = qty.multiply(pm).setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
        BigDecimal yieldSobreCusto = custoAtual.signum() > 0
            ? totalProventos.multiply(BigDecimal.valueOf(100))
                .divide(custoAtual, ESCALA_MONETARIA, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        return new PosicaoResponse(
            FundoResumoResponse.of(fundo),
            qty.intValue(),
            pm.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            custoAtual,
            totalCompras.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            totalVendas.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            lucroRealizado.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            totalProventos.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            yieldSobreCusto,
            operacoes.size(),
            proventos.size()
        );
    }
}
