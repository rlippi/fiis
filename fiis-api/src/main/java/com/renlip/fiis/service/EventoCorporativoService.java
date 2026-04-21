package com.renlip.fiis.service;

import java.util.List;
import java.util.Optional;

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
import com.renlip.fiis.support.UsuarioLogadoSupport;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável pelas regras de negócio de {@link EventoCorporativo}.
 *
 * <p>Principais validações:
 * <ul>
 *   <li>Fundo referenciado deve pertencer ao usuário autenticado;</li>
 *   <li>Fator deve ser positivo (garantido no Bean Validation).</li>
 * </ul>
 * </p>
 *
 * <p><b>Nota:</b> o efeito do evento na posição do investidor é aplicado
 * dinamicamente pelo {@link PosicaoService} ao calcular a posição atual.</p>
 *
 * <p><b>Multi-usuário:</b> USER vê apenas seus eventos; ADMIN tem acesso global.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventoCorporativoService {

    private final EventoCorporativoRepository eventoRepository;
    private final FundoRepository fundoRepository;
    private final EventoCorporativoMapper eventoMapper;
    private final UsuarioLogadoSupport usuarioLogado;

    public List<EventoCorporativoResponse> listarTodos() {
        List<EventoCorporativo> eventos = usuarioLogado.isAdmin()
            ? eventoRepository.findAll()
            : eventoRepository.findByUsuarioId(usuarioLogado.getUsuarioIdAtual());
        return eventoMapper.toResponseList(eventos);
    }

    public List<EventoCorporativoResponse> listarPorFundo(Long fundoId) {
        obterFundoDoUsuario(fundoId);
        return eventoMapper.toResponseList(eventoRepository.findByFundoIdOrderByDataDesc(fundoId));
    }

    public EventoCorporativoResponse buscarPorId(Long id) {
        return eventoMapper.toResponse(obterEntidade(id));
    }

    @Transactional
    public EventoCorporativoResponse criar(EventoCorporativoRequest request) {
        Fundo fundo = obterFundoDoUsuario(request.fundoId());

        EventoCorporativo evento = EventoCorporativo.builder()
            .usuario(fundo.getUsuario())
            .fundo(fundo)
            .tipo(request.tipo())
            .data(request.data())
            .fator(request.fator())
            .descricao(request.descricao())
            .build();

        EventoCorporativo salvo = eventoRepository.save(evento);
        return eventoMapper.toResponse(salvo);
    }

    @Transactional
    public EventoCorporativoResponse atualizar(Long id, EventoCorporativoRequest request) {
        EventoCorporativo evento = obterEntidade(id);
        Fundo fundo = obterFundoDoUsuario(request.fundoId());

        evento.setUsuario(fundo.getUsuario());
        evento.setFundo(fundo);
        evento.setTipo(request.tipo());
        evento.setData(request.data());
        evento.setFator(request.fator());
        evento.setDescricao(request.descricao());

        EventoCorporativo atualizado = eventoRepository.save(evento);
        return eventoMapper.toResponse(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        EventoCorporativo evento = obterEntidade(id);
        eventoRepository.delete(evento);
    }

    private EventoCorporativo obterEntidade(Long id) {
        Optional<EventoCorporativo> evento = usuarioLogado.isAdmin()
            ? eventoRepository.findById(id)
            : eventoRepository.findByIdAndUsuarioId(id, usuarioLogado.getUsuarioIdAtual());

        return evento.orElseThrow(() ->
            new RecursoNaoEncontradoException(MensagemEnum.EVENTO_CORPORATIVO_NAO_ENCONTRADO, id));
    }

    private Fundo obterFundoDoUsuario(Long fundoId) {
        Optional<Fundo> fundo = usuarioLogado.isAdmin()
            ? fundoRepository.findById(fundoId)
            : fundoRepository.findByIdAndUsuarioId(fundoId, usuarioLogado.getUsuarioIdAtual());

        return fundo.orElseThrow(() ->
            new RecursoNaoEncontradoException(MensagemEnum.FUNDO_NAO_ENCONTRADO, fundoId));
    }
}
