package com.renlip.fiis.config.security;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.renlip.fiis.domain.entity.Cotacao;
import com.renlip.fiis.domain.entity.Fundo;
import com.renlip.fiis.domain.entity.Operacao;
import com.renlip.fiis.domain.entity.Provento;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.Segmento;
import com.renlip.fiis.domain.enumeration.TipoFundo;
import com.renlip.fiis.domain.enumeration.TipoOperacao;
import com.renlip.fiis.domain.enumeration.TipoProvento;

/**
 * Helpers que constroem a carteira do usuário demo a partir de definições
 * estáticas. Cada método devolve as entidades já preenchidas (mas ainda
 * desconectadas do EntityManager) — quem persiste é o {@code SeedDemoRunner}.
 *
 * <p>Datas são calculadas a partir de {@link LocalDate#now()} para que o
 * histórico do demo permaneça relevante mesmo que o app fique meses sem
 * receber re-seed (proventos sempre recentes, cotação atualizada).</p>
 */
@Component
public class SeedDemoSupport {

    private static final int MESES_DE_PROVENTOS = 6;

    private record FundoSpec(
        String ticker,
        String nome,
        String cnpj,
        TipoFundo tipo,
        Segmento segmento,
        BigDecimal valorProventoPorCota,
        BigDecimal precoAtual
    ) {}

    private static final List<FundoSpec> FUNDOS_DEMO = List.of(
        new FundoSpec("HGLG11", "CSHG Logística FII", "11728688000147",
            TipoFundo.TIJOLO, Segmento.LOGISTICA,
            new BigDecimal("1.10"), new BigDecimal("158.50")),
        new FundoSpec("KNRI11", "Kinea Renda Imobiliária FII", "12005956000167",
            TipoFundo.TIJOLO, Segmento.LAJES_CORPORATIVAS,
            new BigDecimal("1.05"), new BigDecimal("145.80")),
        new FundoSpec("MXRF11", "Maxi Renda FII", "97521225000140",
            TipoFundo.PAPEL, Segmento.RECEBIVEIS,
            new BigDecimal("0.11"), new BigDecimal("10.55")),
        new FundoSpec("KFOF11", "Kinea FoF FII", "32325054000110",
            TipoFundo.FUNDO_DE_FUNDOS, Segmento.FUNDO_DE_FUNDOS,
            new BigDecimal("0.75"), new BigDecimal("92.30"))
    );

    private record OperacaoSpec(
        String ticker,
        TipoOperacao tipo,
        int mesesAtras,
        int quantidade,
        BigDecimal precoUnitario,
        BigDecimal taxas
    ) {}

    private static final List<OperacaoSpec> OPERACOES_DEMO = List.of(
        // HGLG11 — 30 cotas em 2 compras
        new OperacaoSpec("HGLG11", TipoOperacao.COMPRA, 6, 20, new BigDecimal("148.50"), new BigDecimal("0.50")),
        new OperacaoSpec("HGLG11", TipoOperacao.COMPRA, 4, 10, new BigDecimal("152.00"), new BigDecimal("0.30")),
        // KNRI11 — 20 cotas em 2 compras
        new OperacaoSpec("KNRI11", TipoOperacao.COMPRA, 6, 15, new BigDecimal("140.00"), new BigDecimal("0.50")),
        new OperacaoSpec("KNRI11", TipoOperacao.COMPRA, 3, 5,  new BigDecimal("143.50"), new BigDecimal("0.30")),
        // MXRF11 — 200 cotas em 1 compra
        new OperacaoSpec("MXRF11", TipoOperacao.COMPRA, 5, 200, new BigDecimal("10.20"), new BigDecimal("0.50")),
        // KFOF11 — 15 cotas em 1 compra
        new OperacaoSpec("KFOF11", TipoOperacao.COMPRA, 4, 15, new BigDecimal("88.00"), new BigDecimal("0.50"))
    );

    public List<Fundo> criarFundos(Usuario usuario) {
        return FUNDOS_DEMO.stream()
            .map(spec -> Fundo.builder()
                .usuario(usuario)
                .ticker(spec.ticker())
                .nome(spec.nome())
                .cnpj(spec.cnpj())
                .tipo(spec.tipo())
                .segmento(spec.segmento())
                .ativo(true)
                .build())
            .toList();
    }

    public List<Operacao> criarOperacoes(Usuario usuario, List<Fundo> fundos) {
        LocalDate hoje = LocalDate.now();
        return OPERACOES_DEMO.stream()
            .map(spec -> Operacao.builder()
                .usuario(usuario)
                .fundo(localizarPorTicker(fundos, spec.ticker()))
                .tipo(spec.tipo())
                .dataOperacao(hoje.minusMonths(spec.mesesAtras()))
                .quantidade(spec.quantidade())
                .precoUnitario(spec.precoUnitario())
                .taxas(spec.taxas())
                .build())
            .toList();
    }

    public List<Provento> criarProventos(Usuario usuario, List<Fundo> fundos) {
        LocalDate hoje = LocalDate.now();
        List<Provento> proventos = new ArrayList<>();
        for (FundoSpec spec : FUNDOS_DEMO) {
            Fundo fundo = localizarPorTicker(fundos, spec.ticker());
            int cotasAtuais = totalCotasComprado(spec.ticker());
            for (int i = 1; i <= MESES_DE_PROVENTOS; i++) {
                LocalDate referencia = hoje.minusMonths(i).withDayOfMonth(1).plusMonths(1).minusDays(1);
                LocalDate pagamento = referencia.plusDays(15);
                proventos.add(Provento.builder()
                    .usuario(usuario)
                    .fundo(fundo)
                    .tipoProvento(TipoProvento.RENDIMENTO)
                    .dataReferencia(referencia)
                    .dataPagamento(pagamento)
                    .valorPorCota(spec.valorProventoPorCota())
                    .quantidadeCotas(cotasAtuais)
                    .build());
            }
        }
        return proventos;
    }

    public List<Cotacao> criarCotacoes(Usuario usuario, List<Fundo> fundos) {
        LocalDate dataPregao = LocalDate.now().minusDays(1);
        return FUNDOS_DEMO.stream()
            .map(spec -> Cotacao.builder()
                .usuario(usuario)
                .fundo(localizarPorTicker(fundos, spec.ticker()))
                .data(dataPregao)
                .precoFechamento(spec.precoAtual())
                .build())
            .toList();
    }

    private Fundo localizarPorTicker(List<Fundo> fundos, String ticker) {
        return fundos.stream()
            .filter(f -> f.getTicker().equals(ticker))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Fundo demo '" + ticker + "' não foi criado antes de operações/proventos/cotações."));
    }

    private int totalCotasComprado(String ticker) {
        return OPERACOES_DEMO.stream()
            .filter(op -> op.ticker().equals(ticker))
            .mapToInt(op -> op.tipo() == TipoOperacao.COMPRA ? op.quantidade() : -op.quantidade())
            .sum();
    }
}
