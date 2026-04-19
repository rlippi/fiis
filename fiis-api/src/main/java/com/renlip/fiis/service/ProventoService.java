package com.renlip.fiis.service;

import java.time.LocalDate;
import java.util.List;

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

import lombok.RequiredArgsConstructor;

/**
 * Service responsável pelas regras de negócio de {@link Provento}.
 *
 * <p>Principais validações:
 * <ul>
 *   <li>Fundo referenciado deve existir;</li>
 *   <li>Data de pagamento não pode ser anterior à data de referência.</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProventoService {

    private final ProventoRepository proventoRepository;
    private final FundoRepository fundoRepository;
    private final ProventoMapper proventoMapper;

    /**
     * Lista todos os proventos cadastrados.
     *
     * @return lista completa
     */
    public List<ProventoResponse> listarTodos() {
        return proventoMapper.toResponseList(proventoRepository.findAll());
    }

    /**
     * Lista os proventos de um fundo específico (mais recente primeiro).
     *
     * @param fundoId ID do fundo
     * @return lista de proventos
     * @throws RecursoNaoEncontradoException se o fundo não existir
     */
    public List<ProventoResponse> listarPorFundo(Long fundoId) {
        validarFundoExiste(fundoId);
        return proventoMapper.toResponseList(proventoRepository.findByFundoIdOrderByDataReferenciaDesc(fundoId));
    }

    /**
     * Lista os proventos pagos dentro do intervalo informado (inclusivo).
     *
     * <p>Útil para relatórios de renda passiva mensal/anual.</p>
     *
     * @param inicio data de pagamento inicial
     * @param fim    data de pagamento final
     * @return lista de proventos no período
     * @throws RegraNegocioException se {@code inicio > fim}
     */
    public List<ProventoResponse> listarPorPeriodoPagamento(LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException(MensagemEnum.PERIODO_INVALIDO, inicio, fim);
        }
        return proventoMapper.toResponseList(
            proventoRepository.findByDataPagamentoBetweenOrderByDataPagamentoDesc(inicio, fim));
    }

    /**
     * Busca um provento pelo ID.
     *
     * @param id identificador
     * @return provento encontrado
     * @throws RecursoNaoEncontradoException se não existir
     */
    public ProventoResponse buscarPorId(Long id) {
        return proventoMapper.toResponse(obterEntidade(id));
    }

    /**
     * Cria um novo provento.
     *
     * @param request dados do provento
     * @return provento criado
     * @throws RecursoNaoEncontradoException se o fundo não existir
     * @throws RegraNegocioException         se datas forem incoerentes
     */
    @Transactional
    public ProventoResponse criar(ProventoRequest request) {
        Fundo fundo = obterFundo(request.fundoId());
        validarCoerenciaDatas(request.dataReferencia(), request.dataPagamento());

        Provento provento = Provento.builder()
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

    /**
     * Atualiza um provento existente.
     *
     * @param id      identificador
     * @param request novos dados
     * @return provento atualizado
     * @throws RecursoNaoEncontradoException se o provento ou fundo não existirem
     * @throws RegraNegocioException         se datas forem incoerentes
     */
    @Transactional
    public ProventoResponse atualizar(Long id, ProventoRequest request) {
        Provento provento = obterEntidade(id);
        Fundo fundo = obterFundo(request.fundoId());
        validarCoerenciaDatas(request.dataReferencia(), request.dataPagamento());

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

    /**
     * Remove um provento (hard delete).
     *
     * @param id identificador
     * @throws RecursoNaoEncontradoException se não existir
     */
    @Transactional
    public void deletar(Long id) {
        Provento provento = obterEntidade(id);
        proventoRepository.delete(provento);
    }

    /**
     * Garante que a data de pagamento seja igual ou posterior à data de referência.
     */
    private void validarCoerenciaDatas(LocalDate dataReferencia, LocalDate dataPagamento) {
        if (dataPagamento.isBefore(dataReferencia)) {
            throw new RegraNegocioException(
                MensagemEnum.PROVENTO_DATA_PAGAMENTO_ANTERIOR_REFERENCIA, dataPagamento, dataReferencia);
        }
    }

    private Provento obterEntidade(Long id) {
        return proventoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(MensagemEnum.PROVENTO_NAO_ENCONTRADO, id));
    }

    private Fundo obterFundo(Long fundoId) {
        return fundoRepository.findById(fundoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException(MensagemEnum.FUNDO_NAO_ENCONTRADO, fundoId));
    }

    private void validarFundoExiste(Long fundoId) {
        if (!fundoRepository.existsById(fundoId)) {
            throw new RecursoNaoEncontradoException(MensagemEnum.FUNDO_NAO_ENCONTRADO, fundoId);
        }
    }
}
