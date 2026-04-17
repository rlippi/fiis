package com.renlip.fiis.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.domain.model.Cotacao;
import com.renlip.fiis.domain.model.Fundo;
import com.renlip.fiis.domain.repository.CotacaoRepository;
import com.renlip.fiis.domain.repository.FundoRepository;
import com.renlip.fiis.dto.CotacaoResponse;
import com.renlip.fiis.exception.RecursoNaoEncontradoException;
import com.renlip.fiis.exception.RegraNegocioException;
import com.renlip.fiis.vo.CotacaoRequest;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável pelas regras de negócio de {@link Cotacao}.
 *
 * <p>Principais validações:
 * <ul>
 *   <li>Fundo referenciado deve existir;</li>
 *   <li>Não pode haver duas cotações do mesmo fundo na mesma data;</li>
 *   <li>Se {@code precoMinimo} e {@code precoMaximo} forem informados,
 *       mínimo deve ser ≤ máximo.</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CotacaoService {

    private final CotacaoRepository cotacaoRepository;
    private final FundoRepository fundoRepository;

    /**
     * Lista todas as cotações cadastradas.
     *
     * @return lista completa
     */
    public List<CotacaoResponse> listarTodas() {
        return cotacaoRepository.findAll().stream()
            .map(CotacaoResponse::of)
            .toList();
    }

    /**
     * Lista as cotações de um fundo específico (mais recente primeiro).
     *
     * @param fundoId ID do fundo
     * @return lista de cotações
     * @throws RecursoNaoEncontradoException se o fundo não existir
     */
    public List<CotacaoResponse> listarPorFundo(Long fundoId) {
        validarFundoExiste(fundoId);
        return cotacaoRepository.findByFundoIdOrderByDataDesc(fundoId).stream()
            .map(CotacaoResponse::of)
            .toList();
    }

    /**
     * Busca uma cotação pelo ID.
     *
     * @param id identificador
     * @return cotação encontrada
     * @throws RecursoNaoEncontradoException se não existir
     */
    public CotacaoResponse buscarPorId(Long id) {
        return CotacaoResponse.of(obterEntidade(id));
    }

    /**
     * Retorna a cotação mais recente de um fundo.
     *
     * @param fundoId ID do fundo
     * @return {@link Optional} com a última cotação, ou vazio se nunca houve cotação
     * @throws RecursoNaoEncontradoException se o fundo não existir
     */
    public Optional<CotacaoResponse> buscarUltimaCotacao(Long fundoId) {
        validarFundoExiste(fundoId);
        return cotacaoRepository.findFirstByFundoIdOrderByDataDesc(fundoId)
            .map(CotacaoResponse::of);
    }

    /**
     * Cria uma nova cotação.
     *
     * @param request dados da cotação
     * @return cotação criada
     * @throws RecursoNaoEncontradoException se o fundo não existir
     * @throws RegraNegocioException         se já houver cotação para o fundo/data
     *                                       ou se mínimo &gt; máximo
     */
    @Transactional
    public CotacaoResponse criar(CotacaoRequest request) {
        Fundo fundo = obterFundo(request.fundoId());
        validarMinimoMaximo(request.precoMinimo(), request.precoMaximo());

        if (cotacaoRepository.existsByFundoIdAndData(fundo.getId(), request.data())) {
            throw new RegraNegocioException(
                "Já existe cotação do fundo " + fundo.getTicker() +
                " na data " + request.data());
        }

        Cotacao cotacao = Cotacao.builder()
            .fundo(fundo)
            .data(request.data())
            .precoFechamento(request.precoFechamento())
            .precoAbertura(request.precoAbertura())
            .precoMinimo(request.precoMinimo())
            .precoMaximo(request.precoMaximo())
            .volume(request.volume())
            .build();

        Cotacao salva = cotacaoRepository.save(cotacao);
        return CotacaoResponse.of(salva);
    }

    /**
     * Atualiza uma cotação existente.
     *
     * @param id      identificador
     * @param request novos dados
     * @return cotação atualizada
     * @throws RecursoNaoEncontradoException se a cotação ou o fundo não existirem
     * @throws RegraNegocioException         se violar unicidade ou coerência
     */
    @Transactional
    public CotacaoResponse atualizar(Long id, CotacaoRequest request) {
        Cotacao cotacao = obterEntidade(id);
        Fundo fundo = obterFundo(request.fundoId());
        validarMinimoMaximo(request.precoMinimo(), request.precoMaximo());

        boolean mudouChave = !cotacao.getFundo().getId().equals(fundo.getId())
            || !cotacao.getData().equals(request.data());

        if (mudouChave && cotacaoRepository.existsByFundoIdAndData(fundo.getId(), request.data())) {
            throw new RegraNegocioException(
                "Já existe cotação do fundo " + fundo.getTicker() +
                " na data " + request.data());
        }

        cotacao.setFundo(fundo);
        cotacao.setData(request.data());
        cotacao.setPrecoFechamento(request.precoFechamento());
        cotacao.setPrecoAbertura(request.precoAbertura());
        cotacao.setPrecoMinimo(request.precoMinimo());
        cotacao.setPrecoMaximo(request.precoMaximo());
        cotacao.setVolume(request.volume());

        Cotacao atualizada = cotacaoRepository.save(cotacao);
        return CotacaoResponse.of(atualizada);
    }

    /**
     * Remove uma cotação (hard delete).
     *
     * @param id identificador
     * @throws RecursoNaoEncontradoException se não existir
     */
    @Transactional
    public void deletar(Long id) {
        Cotacao cotacao = obterEntidade(id);
        cotacaoRepository.delete(cotacao);
    }

    /**
     * Valida que o preço mínimo é menor ou igual ao máximo, se ambos informados.
     */
    private void validarMinimoMaximo(BigDecimal minimo, BigDecimal maximo) {
        if (minimo != null && maximo != null && minimo.compareTo(maximo) > 0) {
            throw new RegraNegocioException(
                "Preço mínimo (" + minimo + ") não pode ser maior que o preço máximo (" + maximo + ")");
        }
    }

    private Cotacao obterEntidade(Long id) {
        return cotacaoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Cotação com ID " + id + " não encontrada"));
    }

    private Fundo obterFundo(Long fundoId) {
        return fundoRepository.findById(fundoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Fundo com ID " + fundoId + " não encontrado"));
    }

    private void validarFundoExiste(Long fundoId) {
        if (!fundoRepository.existsById(fundoId)) {
            throw new RecursoNaoEncontradoException(
                "Fundo com ID " + fundoId + " não encontrado");
        }
    }
}
