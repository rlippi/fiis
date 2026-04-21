package com.renlip.fiis.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.domain.dto.ProventoResponse;
import com.renlip.fiis.domain.entity.Fundo;
import com.renlip.fiis.domain.entity.Provento;
import com.renlip.fiis.domain.enumeration.MensagemEnum;
import com.renlip.fiis.domain.mapper.ProventoMapper;
import com.renlip.fiis.domain.vo.ProventoRequest;
import com.renlip.fiis.exception.RecursoNaoEncontradoException;
import com.renlip.fiis.exception.RegraNegocioException;
import com.renlip.fiis.repository.FundoRepository;
import com.renlip.fiis.repository.ProventoRepository;
import com.renlip.fiis.support.UsuarioLogadoSupport;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável pelas regras de negócio de {@link Provento}.
 *
 * <p>Multi-usuário: USER vê apenas seus proventos; ADMIN tem acesso global.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProventoService {

    private final ProventoRepository proventoRepository;
    private final FundoRepository fundoRepository;
    private final ProventoMapper proventoMapper;
    private final UsuarioLogadoSupport usuarioLogado;

    public List<ProventoResponse> listarTodos() {
        List<Provento> proventos = usuarioLogado.isAdmin()
            ? proventoRepository.findAll()
            : proventoRepository.findByUsuarioId(usuarioLogado.getUsuarioIdAtual());
        return proventoMapper.toResponseList(proventos);
    }

    public List<ProventoResponse> listarPorFundo(Long fundoId) {
        obterFundoDoUsuario(fundoId);
        return proventoMapper.toResponseList(proventoRepository.findByFundoIdOrderByDataReferenciaDesc(fundoId));
    }

    /**
     * Lista os proventos pagos dentro do intervalo informado (inclusivo).
     * Respeita filtro de usuário.
     */
    public List<ProventoResponse> listarPorPeriodoPagamento(LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException(MensagemEnum.PERIODO_INVALIDO, inicio, fim);
        }
        List<Provento> proventos = usuarioLogado.isAdmin()
            ? proventoRepository.findByDataPagamentoBetweenOrderByDataPagamentoDesc(inicio, fim)
            : proventoRepository.findByUsuarioIdAndDataPagamentoBetweenOrderByDataPagamentoDesc(
                usuarioLogado.getUsuarioIdAtual(), inicio, fim);
        return proventoMapper.toResponseList(proventos);
    }

    public ProventoResponse buscarPorId(Long id) {
        return proventoMapper.toResponse(obterEntidade(id));
    }

    @Transactional
    public ProventoResponse criar(ProventoRequest request) {
        Fundo fundo = obterFundoDoUsuario(request.fundoId());
        validarCoerenciaDatas(request.dataReferencia(), request.dataPagamento());

        Provento provento = Provento.builder()
            .usuario(fundo.getUsuario())
            .fundo(fundo)
            .tipoProvento(request.tipoProvento())
            .dataReferencia(request.dataReferencia())
            .dataPagamento(request.dataPagamento())
            .valorPorCota(request.valorPorCota())
            .quantidadeCotas(request.quantidadeCotas())
            .observacao(request.observacao())
            .build();

        Provento salvo = proventoRepository.save(provento);
        return proventoMapper.toResponse(salvo);
    }

    @Transactional
    public ProventoResponse atualizar(Long id, ProventoRequest request) {
        Provento provento = obterEntidade(id);
        Fundo fundo = obterFundoDoUsuario(request.fundoId());
        validarCoerenciaDatas(request.dataReferencia(), request.dataPagamento());

        provento.setUsuario(fundo.getUsuario());
        provento.setFundo(fundo);
        provento.setTipoProvento(request.tipoProvento());
        provento.setDataReferencia(request.dataReferencia());
        provento.setDataPagamento(request.dataPagamento());
        provento.setValorPorCota(request.valorPorCota());
        provento.setQuantidadeCotas(request.quantidadeCotas());
        provento.setObservacao(request.observacao());

        Provento atualizado = proventoRepository.save(provento);
        return proventoMapper.toResponse(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        Provento provento = obterEntidade(id);
        proventoRepository.delete(provento);
    }

    private void validarCoerenciaDatas(LocalDate dataReferencia, LocalDate dataPagamento) {
        if (dataPagamento.isBefore(dataReferencia)) {
            throw new RegraNegocioException(
                MensagemEnum.PROVENTO_DATA_PAGAMENTO_ANTERIOR_REFERENCIA, dataPagamento, dataReferencia);
        }
    }

    private Provento obterEntidade(Long id) {
        Optional<Provento> provento = usuarioLogado.isAdmin()
            ? proventoRepository.findById(id)
            : proventoRepository.findByIdAndUsuarioId(id, usuarioLogado.getUsuarioIdAtual());

        return provento.orElseThrow(() ->
            new RecursoNaoEncontradoException(MensagemEnum.PROVENTO_NAO_ENCONTRADO, id));
    }

    private Fundo obterFundoDoUsuario(Long fundoId) {
        Optional<Fundo> fundo = usuarioLogado.isAdmin()
            ? fundoRepository.findById(fundoId)
            : fundoRepository.findByIdAndUsuarioId(fundoId, usuarioLogado.getUsuarioIdAtual());

        return fundo.orElseThrow(() ->
            new RecursoNaoEncontradoException(MensagemEnum.FUNDO_NAO_ENCONTRADO, fundoId));
    }
}
