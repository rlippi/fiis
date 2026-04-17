package com.renlip.fiis.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.domain.dto.EventoCorporativoResponse;
import com.renlip.fiis.domain.entity.EventoCorporativo;
import com.renlip.fiis.domain.entity.Fundo;
import com.renlip.fiis.domain.enumeration.MensagemEnum;
import com.renlip.fiis.domain.mapper.EventoCorporativoMapper;
import com.renlip.fiis.domain.vo.EventoCorporativoRequest;
import com.renlip.fiis.exception.RecursoNaoEncontradoException;
import com.renlip.fiis.repository.EventoCorporativoRepository;
import com.renlip.fiis.repository.FundoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável pelas regras de negócio de {@link EventoCorporativo}.
 *
 * <p>Principais validações:
 * <ul>
 *   <li>Fundo referenciado deve existir;</li>
 *   <li>Fator deve ser positivo (garantido no Bean Validation).</li>
 * </ul>
 * </p>
 *
 * <p><b>Nota:</b> o efeito do evento na posição do investidor é aplicado
 * dinamicamente pelo {@link PosicaoService} ao calcular a posição atual.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventoCorporativoService {

    private final EventoCorporativoRepository eventoRepository;
    private final FundoRepository fundoRepository;
    private final EventoCorporativoMapper eventoMapper;

    /**
     * Lista todos os eventos corporativos cadastrados.
     *
     * @return lista completa
     */
    public List<EventoCorporativoResponse> listarTodos() {
        return eventoMapper.toResponseList(eventoRepository.findAll());
    }

    /**
     * Lista os eventos corporativos de um fundo específico (mais recente primeiro).
     *
     * @param fundoId ID do fundo
     * @return lista de eventos
     * @throws RecursoNaoEncontradoException se o fundo não existir
     */
    public List<EventoCorporativoResponse> listarPorFundo(Long fundoId) {
        validarFundoExiste(fundoId);
        return eventoMapper.toResponseList(eventoRepository.findByFundoIdOrderByDataDesc(fundoId));
    }

    /**
     * Busca um evento pelo ID.
     *
     * @param id identificador
     * @return evento encontrado
     * @throws RecursoNaoEncontradoException se não existir
     */
    public EventoCorporativoResponse buscarPorId(Long id) {
        return eventoMapper.toResponse(obterEntidade(id));
    }

    /**
     * Cria um novo evento corporativo.
     *
     * @param request dados do evento
     * @return evento criado
     * @throws RecursoNaoEncontradoException se o fundo não existir
     */
    @Transactional
    public EventoCorporativoResponse criar(EventoCorporativoRequest request) {
        Fundo fundo = obterFundo(request.fundoId());

        EventoCorporativo evento = EventoCorporativo.builder()
            .fundo(fundo)
            .tipo(request.tipo())
            .data(request.data())
            .fator(request.fator())
            .descricao(request.descricao())
            .build();

        EventoCorporativo salvo = eventoRepository.save(evento);
        return eventoMapper.toResponse(salvo);
    }

    /**
     * Atualiza um evento corporativo existente.
     *
     * @param id      identificador
     * @param request novos dados
     * @return evento atualizado
     * @throws RecursoNaoEncontradoException se o evento ou fundo não existirem
     */
    @Transactional
    public EventoCorporativoResponse atualizar(Long id, EventoCorporativoRequest request) {
        EventoCorporativo evento = obterEntidade(id);
        Fundo fundo = obterFundo(request.fundoId());

        evento.setFundo(fundo);
        evento.setTipo(request.tipo());
        evento.setData(request.data());
        evento.setFator(request.fator());
        evento.setDescricao(request.descricao());

        EventoCorporativo atualizado = eventoRepository.save(evento);
        return eventoMapper.toResponse(atualizado);
    }

    /**
     * Remove um evento corporativo (hard delete).
     *
     * @param id identificador
     * @throws RecursoNaoEncontradoException se não existir
     */
    @Transactional
    public void deletar(Long id) {
        EventoCorporativo evento = obterEntidade(id);
        eventoRepository.delete(evento);
    }

    private EventoCorporativo obterEntidade(Long id) {
        return eventoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(MensagemEnum.EVENTO_CORPORATIVO_NAO_ENCONTRADO, id));
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
