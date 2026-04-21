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

import com.renlip.fiis.domain.dto.AlocacaoResponse;
import com.renlip.fiis.domain.dto.PosicaoResponse;
import com.renlip.fiis.domain.dto.RendaMensalResponse;
import com.renlip.fiis.domain.dto.RendaPorFundoResponse;
import com.renlip.fiis.domain.dto.ResumoCarteiraResponse;
import com.renlip.fiis.domain.entity.Fundo;
import com.renlip.fiis.domain.mapper.FundoResumoMapper;
import com.renlip.fiis.repository.FundoRepository;
import com.renlip.fiis.repository.ProventoRepository;
import com.renlip.fiis.support.UsuarioLogadoSupport;

import static com.renlip.fiis.constant.EscalaConstants.ESCALA_MONETARIA;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável por relatórios e agregações de dados da carteira.
 *
 * <p>Agrupa operações, proventos e posições para gerar visões resumidas
 * que alimentam o dashboard do frontend.</p>
 *
 * <p><b>Multi-usuário:</b> todas as agregações são sempre no escopo do usuário
 * autenticado — admin inclusive vê apenas a própria carteira no dashboard.
 * Consolidação global não é um caso de uso desta versão.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatorioService {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private final ProventoRepository proventoRepository;
    private final FundoRepository fundoRepository;
    private final PosicaoService posicaoService;
    private final FundoResumoMapper fundoResumoMapper;
    private final UsuarioLogadoSupport usuarioLogado;

    public List<RendaMensalResponse> gerarRendaMensal() {
        return proventoRepository.agregarRendaMensalPorUsuario(usuarioLogado.getUsuarioIdAtual()).stream()
            .map(this::mapearRendaMensal)
            .toList();
    }

    public List<RendaPorFundoResponse> gerarRendaPorFundo() {
        List<Object[]> linhas = proventoRepository.agregarRendaPorFundoPorUsuario(usuarioLogado.getUsuarioIdAtual());

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
                    fundoResumoMapper.toResponse(fundosPorId.get(fundoId)),
                    total,
                    quantidade
                );
            })
            .toList();
    }

    public ResumoCarteiraResponse gerarResumoCarteira() {
        Long usuarioId = usuarioLogado.getUsuarioIdAtual();
        List<Fundo> fundosAtivos = fundoRepository.findByUsuarioIdAndAtivoTrue(usuarioId);

        List<PosicaoResponse> posicoes = fundosAtivos.stream()
            .map(f -> posicaoService.calcularPosicaoDoFundo(f.getId()))
            .toList();

        BigDecimal custoTotal = somar(posicoes, PosicaoResponse::custoAtual);
        BigDecimal totalCompras = somar(posicoes, PosicaoResponse::totalCompras);
        BigDecimal totalVendas = somar(posicoes, PosicaoResponse::totalVendas);
        BigDecimal lucroRealizado = somar(posicoes, PosicaoResponse::lucroRealizado);
        BigDecimal totalProventos = somar(posicoes, PosicaoResponse::totalProventos);
        BigDecimal valorTotal = somar(posicoes, PosicaoResponse::valorAtual);

        int fundosComPosicao = (int) posicoes.stream()
            .filter(p -> p.quantidadeCotas() > 0)
            .count();

        int mesesComProventos = proventoRepository.agregarRendaMensalPorUsuario(usuarioId).size();

        BigDecimal yield = custoTotal.signum() > 0
            ? totalProventos.multiply(BigDecimal.valueOf(100))
                .divide(custoTotal, ESCALA_MONETARIA, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal mediaMensal = mesesComProventos > 0
            ? totalProventos.divide(BigDecimal.valueOf(mesesComProventos),
                ESCALA_MONETARIA, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal variacaoPatrimonial = custoTotal.signum() > 0 && valorTotal.signum() > 0
            ? valorTotal.subtract(custoTotal).multiply(BigDecimal.valueOf(100))
                .divide(custoTotal, ESCALA_MONETARIA, RoundingMode.HALF_UP)
            : null;

        BigDecimal dyCarteira = valorTotal.signum() > 0
            ? totalProventos.multiply(BigDecimal.valueOf(100))
                .divide(valorTotal, ESCALA_MONETARIA, RoundingMode.HALF_UP)
            : null;

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
            mesesComProventos,
            valorTotal.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP),
            variacaoPatrimonial,
            dyCarteira
        );
    }

    private BigDecimal somar(List<PosicaoResponse> posicoes,
                             Function<PosicaoResponse, BigDecimal> extrator) {
        return posicoes.stream()
            .map(extrator)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<AlocacaoResponse> gerarAlocacaoPorTipo() {
        return gerarAlocacao(
            f -> f.getTipo().name(),
            f -> f.getTipo().getDescricao()
        );
    }

    public List<AlocacaoResponse> gerarAlocacaoPorSegmento() {
        return gerarAlocacao(
            f -> f.getSegmento().name(),
            f -> f.getSegmento().getDescricao()
        );
    }

    private List<AlocacaoResponse> gerarAlocacao(Function<Fundo, String> chaveCodigoFn,
                                                 Function<Fundo, String> chaveDescricaoFn) {
        List<Fundo> fundos = fundoRepository.findByUsuarioIdAndAtivoTrue(usuarioLogado.getUsuarioIdAtual());

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

    private Integer toInteger(Object valor) {
        return ((Number) valor).intValue();
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
