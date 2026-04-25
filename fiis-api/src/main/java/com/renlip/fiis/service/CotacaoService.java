package com.renlip.fiis.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import com.renlip.fiis.domain.dto.CotacaoResponse;
import com.renlip.fiis.domain.dto.ImportacaoBrapiResponse;
import com.renlip.fiis.domain.dto.brapi.BrapiQuote;
import com.renlip.fiis.domain.dto.brapi.BrapiQuoteResponse;
import com.renlip.fiis.domain.entity.Cotacao;
import com.renlip.fiis.domain.entity.Fundo;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.MensagemEnum;
import com.renlip.fiis.domain.mapper.CotacaoMapper;
import com.renlip.fiis.domain.vo.CotacaoRequest;
import com.renlip.fiis.exception.RecursoNaoEncontradoException;
import com.renlip.fiis.exception.RegraNegocioException;
import com.renlip.fiis.repository.CotacaoRepository;
import com.renlip.fiis.repository.FundoRepository;
import com.renlip.fiis.support.BrapiClient;
import com.renlip.fiis.support.UsuarioLogadoSupport;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável pelas regras de negócio de {@link Cotacao}.
 *
 * <p>Principais validações:
 * <ul>
 *   <li>Fundo referenciado deve pertencer ao usuário autenticado;</li>
 *   <li>Não pode haver duas cotações do mesmo fundo na mesma data;</li>
 *   <li>Se {@code precoMinimo} e {@code precoMaximo} forem informados,
 *       mínimo deve ser ≤ máximo.</li>
 * </ul>
 * </p>
 *
 * <p><b>Multi-usuário:</b> USER vê apenas suas cotações; ADMIN tem acesso global.
 * A importação BRAPI ocorre sempre no contexto do usuário autenticado — ADMIN
 * importa a sua própria carteira.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CotacaoService {

    private final CotacaoRepository cotacaoRepository;
    private final FundoRepository fundoRepository;
    private final CotacaoMapper cotacaoMapper;
    private final BrapiClient brapiClient;
    private final UsuarioLogadoSupport usuarioLogado;

    public List<CotacaoResponse> listarTodas() {
        List<Cotacao> cotacoes = usuarioLogado.isAdmin()
            ? cotacaoRepository.findAll()
            : cotacaoRepository.findByUsuarioId(usuarioLogado.getUsuarioIdAtual());
        return cotacaoMapper.toResponseList(cotacoes);
    }

    public List<CotacaoResponse> listarPorFundo(Long fundoId) {
        obterFundoDoUsuario(fundoId);
        return cotacaoMapper.toResponseList(cotacaoRepository.findByFundoIdOrderByDataDesc(fundoId));
    }

    public CotacaoResponse buscarPorId(Long id) {
        return cotacaoMapper.toResponse(obterEntidade(id));
    }

    public Optional<CotacaoResponse> buscarUltimaCotacao(Long fundoId) {
        obterFundoDoUsuario(fundoId);
        return cotacaoRepository.findFirstByFundoIdOrderByDataDesc(fundoId)
            .map(cotacaoMapper::toResponse);
    }

    @Transactional
    public CotacaoResponse criar(CotacaoRequest request) {
        Fundo fundo = obterFundoDoUsuario(request.fundoId());
        validarMinimoMaximo(request.precoMinimo(), request.precoMaximo());

        if (cotacaoRepository.existsByFundoIdAndData(fundo.getId(), request.data())) {
            throw new RegraNegocioException(
                MensagemEnum.COTACAO_JA_EXISTE_NO_PERIODO, fundo.getTicker(), request.data());
        }

        Cotacao cotacao = Cotacao.builder()
            .usuario(fundo.getUsuario())
            .fundo(fundo)
            .data(request.data())
            .precoFechamento(request.precoFechamento())
            .precoAbertura(request.precoAbertura())
            .precoMinimo(request.precoMinimo())
            .precoMaximo(request.precoMaximo())
            .volume(request.volume())
            .build();

        Cotacao salva = cotacaoRepository.save(cotacao);
        return cotacaoMapper.toResponse(salva);
    }

    @Transactional
    public CotacaoResponse atualizar(Long id, CotacaoRequest request) {
        Cotacao cotacao = obterEntidade(id);
        Fundo fundo = obterFundoDoUsuario(request.fundoId());
        validarMinimoMaximo(request.precoMinimo(), request.precoMaximo());

        boolean mudouChave = !cotacao.getFundo().getId().equals(fundo.getId())
            || !cotacao.getData().equals(request.data());

        if (mudouChave && cotacaoRepository.existsByFundoIdAndData(fundo.getId(), request.data())) {
            throw new RegraNegocioException(
                MensagemEnum.COTACAO_JA_EXISTE_NO_PERIODO, fundo.getTicker(), request.data());
        }

        cotacao.setUsuario(fundo.getUsuario());
        cotacao.setFundo(fundo);
        cotacao.setData(request.data());
        cotacao.setPrecoFechamento(request.precoFechamento());
        cotacao.setPrecoAbertura(request.precoAbertura());
        cotacao.setPrecoMinimo(request.precoMinimo());
        cotacao.setPrecoMaximo(request.precoMaximo());
        cotacao.setVolume(request.volume());

        Cotacao atualizada = cotacaoRepository.save(cotacao);
        return cotacaoMapper.toResponse(atualizada);
    }

    @Transactional
    public void deletar(Long id) {
        Cotacao cotacao = obterEntidade(id);
        cotacaoRepository.delete(cotacao);
    }

    /**
     * Importa cotações da BRAPI para todos os fundos ativos da carteira do usuário
     * autenticado, na data de hoje. ADMIN importa apenas os seus fundos próprios
     * (não da carteira de outros usuários).
     *
     * <p>Comportamento de upsert: se já existe cotação do fundo para hoje,
     * os preços são atualizados com os valores mais recentes da BRAPI. Se não
     * existe, uma nova cotação é criada.</p>
     */
    @Transactional
    public ImportacaoBrapiResponse importarViaBrapi() {
        return importarViaBrapiPara(usuarioLogado.getUsuarioAtual());
    }

    /**
     * Importa cotações da BRAPI para os fundos ativos do usuário informado.
     *
     * <p>Variante sem dependência do {@link UsuarioLogadoSupport} (que só
     * funciona dentro de uma request HTTP autenticada). Projetada para ser
     * chamada por jobs agendados, onde o {@code SecurityContextHolder} está
     * vazio.</p>
     *
     * @param usuario usuário dono da carteira a ser atualizada
     */
    @Transactional
    public ImportacaoBrapiResponse importarViaBrapiPara(Usuario usuario) {
        List<Fundo> fundosAtivos = fundoRepository.findByUsuarioIdAndAtivoTrue(usuario.getId());
        if (fundosAtivos.isEmpty()) {
            throw new RegraNegocioException(MensagemEnum.CARTEIRA_SEM_FUNDOS_ATIVOS);
        }

        List<String> tickers = fundosAtivos.stream().map(Fundo::getTicker).toList();

        BrapiQuoteResponse resposta;
        try {
            resposta = brapiClient.buscarCotacoes(tickers);
        } catch (RestClientException ex) {
            throw new RegraNegocioException(MensagemEnum.COTACAO_BRAPI_INDISPONIVEL);
        }

        Map<String, Fundo> fundosPorTicker = new HashMap<>();
        fundosAtivos.forEach(f -> fundosPorTicker.put(f.getTicker(), f));

        List<String> naoEncontrados = new ArrayList<>(tickers);
        LocalDate hoje = LocalDate.now();
        int criados = 0;
        int atualizados = 0;

        List<BrapiQuote> results = resposta == null || resposta.results() == null
            ? List.of()
            : resposta.results();

        for (BrapiQuote quote : results) {
            Fundo fundo = fundosPorTicker.get(quote.symbol());
            if (fundo == null || quote.regularMarketPrice() == null) {
                continue;
            }
            naoEncontrados.remove(quote.symbol());

            Optional<Cotacao> existente =
                cotacaoRepository.findByFundoIdAndData(fundo.getId(), hoje);

            Cotacao cotacao = existente.orElseGet(() -> Cotacao.builder()
                .usuario(fundo.getUsuario())
                .fundo(fundo)
                .data(hoje)
                .build());

            cotacao.setPrecoFechamento(quote.regularMarketPrice());
            cotacao.setPrecoAbertura(quote.regularMarketOpen());
            cotacao.setPrecoMinimo(quote.regularMarketDayLow());
            cotacao.setPrecoMaximo(quote.regularMarketDayHigh());
            cotacao.setVolume(quote.regularMarketVolume());

            cotacaoRepository.save(cotacao);
            if (existente.isPresent()) {
                atualizados++;
            } else {
                criados++;
            }
        }

        return new ImportacaoBrapiResponse(
            fundosAtivos.size(), criados, atualizados, naoEncontrados);
    }

    private void validarMinimoMaximo(BigDecimal minimo, BigDecimal maximo) {
        if (minimo != null && maximo != null && minimo.compareTo(maximo) > 0) {
            throw new RegraNegocioException(
                MensagemEnum.COTACAO_INTERVALO_PRECO_INVALIDO, minimo, maximo);
        }
    }

    private Cotacao obterEntidade(Long id) {
        Optional<Cotacao> cotacao = usuarioLogado.isAdmin()
            ? cotacaoRepository.findById(id)
            : cotacaoRepository.findByIdAndUsuarioId(id, usuarioLogado.getUsuarioIdAtual());

        return cotacao.orElseThrow(() ->
            new RecursoNaoEncontradoException(MensagemEnum.COTACAO_NAO_ENCONTRADA, id));
    }

    private Fundo obterFundoDoUsuario(Long fundoId) {
        Optional<Fundo> fundo = usuarioLogado.isAdmin()
            ? fundoRepository.findById(fundoId)
            : fundoRepository.findByIdAndUsuarioId(fundoId, usuarioLogado.getUsuarioIdAtual());

        return fundo.orElseThrow(() ->
            new RecursoNaoEncontradoException(MensagemEnum.FUNDO_NAO_ENCONTRADO, fundoId));
    }
}
