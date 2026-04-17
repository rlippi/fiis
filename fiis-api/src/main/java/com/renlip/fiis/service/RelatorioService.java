package com.renlip.fiis.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.domain.model.Fundo;
import com.renlip.fiis.domain.repository.FundoRepository;
import com.renlip.fiis.domain.repository.ProventoRepository;
import com.renlip.fiis.dto.AlocacaoResponse;
import com.renlip.fiis.dto.FundoResumoResponse;
import com.renlip.fiis.dto.PosicaoResponse;
import com.renlip.fiis.dto.RendaMensalResponse;
import com.renlip.fiis.dto.RendaPorFundoResponse;
import com.renlip.fiis.dto.ResumoCarteiraResponse;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável por relatórios e agregações de dados da carteira.
 *
 * <p>Agrupa operações, proventos e posições para gerar visões resumidas
 * que alimentam o dashboard do frontend.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatorioService {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final int ESCALA_MONETARIA = 2;

    private final ProventoRepository proventoRepository;
    private final FundoRepository fundoRepository;
    private final PosicaoService posicaoService;

    /**
     * Retorna a renda passiva consolidada por mês de pagamento.
     *
     * <p>Os dados são ordenados do mês mais recente para o mais antigo,
     * facilitando exibição em gráficos e listas no frontend.</p>
     *
     * @return lista de {@link RendaMensalResponse}
     */
    public List<RendaMensalResponse> gerarRendaMensal() {
        return proventoRepository.agregarRendaMensal().stream()
            .map(this::mapearRendaMensal)
            .toList();
    }

    /**
     * Retorna a renda passiva consolidada por fundo.
     *
     * <p>Carrega todos os fundos referenciados numa única consulta para
     * evitar problema de N+1.</p>
     *
     * @return lista ordenada do maior recebedor ao menor
     */
    public List<RendaPorFundoResponse> gerarRendaPorFundo() {
        List<Object[]> linhas = proventoRepository.agregarRendaPorFundo();

        List<Long> fundoIds = linhas.stream()
            .map(linha -> (Long) linha[0])
            .toList();

        Map<Long, Fundo> fundosPorId = fundoRepository.findAllById(fundoIds).stream()
            .collect(Collectors.toMap(Fundo::getId, Function.identity()));

        return linhas.stream()
            .map(linha -> {
                Long fundoId = (Long) linha[0];
                BigDecimal total = ((BigDecimal) linha[1])
                    .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
                Integer quantidade = ((Number) linha[2]).intValue();
                return new RendaPorFundoResponse(
                    FundoResumoResponse.of(fundosPorId.get(fundoId)),
                    total,
                    quantidade
                );
            })
            .toList();
    }

    /**
     * Retorna o resumo geral da carteira (totais consolidados).
     *
     * <p>Agrega todas as posições dos fundos ativos e todos os proventos
     * para gerar uma visão única de topo do dashboard.</p>
     *
     * @return resumo da carteira
     */
    public ResumoCarteiraResponse gerarResumoCarteira() {
        List<Fundo> fundosAtivos = fundoRepository.findByAtivoTrue();

        List<PosicaoResponse> posicoes = fundosAtivos.stream()
            .map(f -> posicaoService.calcularPosicaoDoFundo(f.getId()))
            .toList();

        BigDecimal custoTotal = somar(posicoes, PosicaoResponse::custoAtual);
        BigDecimal totalCompras = somar(posicoes, PosicaoResponse::totalCompras);
        BigDecimal totalVendas = somar(posicoes, PosicaoResponse::totalVendas);
        BigDecimal lucroRealizado = somar(posicoes, PosicaoResponse::lucroRealizado);
        BigDecimal totalProventos = somar(posicoes, PosicaoResponse::totalProventos);

        int fundosComPosicao = (int) posicoes.stream()
            .filter(p -> p.quantidadeCotas() > 0)
            .count();

        int mesesComProventos = proventoRepository.agregarRendaMensal().size();

        BigDecimal yield = custoTotal.signum() > 0
            ? totalProventos.multiply(BigDecimal.valueOf(100))
                .divide(custoTotal, ESCALA_MONETARIA, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal mediaMensal = mesesComProventos > 0
            ? totalProventos.divide(BigDecimal.valueOf(mesesComProventos),
                ESCALA_MONETARIA, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        return new ResumoCarteiraResponse(
            fundosAtivos.size(),
            fundosComPosicao,
            custoTotal.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            totalCompras.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            totalVendas.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            lucroRealizado.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            totalProventos.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            yield,
            mediaMensal,
            mesesComProventos
        );
    }

    /**
     * Soma os valores de uma propriedade específica de todas as posições.
     */
    private BigDecimal somar(List<PosicaoResponse> posicoes,
                             Function<PosicaoResponse, BigDecimal> extrator) {
        return posicoes.stream()
            .map(extrator)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Retorna a alocação da carteira por {@link com.renlip.fiis.domain.enums.TipoFundo}.
     *
     * @return lista de alocações, ordenada do maior custo ao menor
     */
    public List<AlocacaoResponse> gerarAlocacaoPorTipo() {
        return gerarAlocacao(
            f -> f.getTipo().name(),
            f -> f.getTipo().getDescricao()
        );
    }

    /**
     * Retorna a alocação da carteira por {@link com.renlip.fiis.domain.enums.Segmento}.
     *
     * @return lista de alocações, ordenada do maior custo ao menor
     */
    public List<AlocacaoResponse> gerarAlocacaoPorSegmento() {
        return gerarAlocacao(
            f -> f.getSegmento().name(),
            f -> f.getSegmento().getDescricao()
        );
    }

    /**
     * Agrupa a carteira por uma categoria derivada do fundo (tipo ou segmento)
     * e calcula o custo por categoria, o percentual e o número de fundos.
     *
     * @param chaveCodigoFn    função para extrair o código da categoria (ex: "LOGISTICA")
     * @param chaveDescricaoFn função para extrair a descrição (ex: "Logística")
     * @return lista de alocações
     */
    private List<AlocacaoResponse> gerarAlocacao(Function<Fundo, String> chaveCodigoFn,
                                                 Function<Fundo, String> chaveDescricaoFn) {
        List<Fundo> fundos = fundoRepository.findByAtivoTrue();

        Map<Long, PosicaoResponse> posicoesPorFundoId = fundos.stream()
            .collect(Collectors.toMap(
                Fundo::getId,
                f -> posicaoService.calcularPosicaoDoFundo(f.getId())
            ));

        BigDecimal totalCusto = posicoesPorFundoId.values().stream()
            .map(PosicaoResponse::custoAtual)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, List<Fundo>> agrupado = fundos.stream()
            .collect(Collectors.groupingBy(chaveCodigoFn));

        return agrupado.entrySet().stream()
            .map(entry -> {
                String codigo = entry.getKey();
                List<Fundo> fundosCategoria = entry.getValue();
                String descricao = chaveDescricaoFn.apply(fundosCategoria.get(0));

                BigDecimal somaCusto = fundosCategoria.stream()
                    .map(f -> posicoesPorFundoId.get(f.getId()).custoAtual())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal percentual = totalCusto.signum() > 0
                    ? somaCusto.multiply(BigDecimal.valueOf(100))
                        .divide(totalCusto, ESCALA_MONETARIA, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

                return new AlocacaoResponse(
                    codigo,
                    descricao,
                    somaCusto.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
                    percentual,
                    fundosCategoria.size()
                );
            })
            .sorted(Comparator.comparing(AlocacaoResponse::custoAtual).reversed())
            .toList();
    }

    /**
     * Converte uma linha ({@code Object[]}) vinda do agrupamento SQL em
     * {@link RendaMensalResponse}, capitalizando o nome do mês em português.
     */
    private RendaMensalResponse mapearRendaMensal(Object[] linha) {
        Integer ano = toInteger(linha[0]);
        Integer mes = toInteger(linha[1]);
        BigDecimal total = ((BigDecimal) linha[2])
            .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
        Integer quantidade = ((Number) linha[3]).intValue();

        return new RendaMensalResponse(
            ano,
            mes,
            capitalizar(Month.of(mes).getDisplayName(TextStyle.FULL, PT_BR)),
            total,
            quantidade
        );
    }

    /**
     * Converte o valor retornado pelo banco em {@link Integer}.
     * O Hibernate pode trazer o resultado de {@code YEAR()/MONTH()} como
     * Integer, Long ou outro {@link Number} — essa normalização evita
     * {@link ClassCastException}.
     */
    private Integer toInteger(Object valor) {
        return ((Number) valor).intValue();
    }

    /**
     * Converte "abril" em "Abril".
     */
    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
