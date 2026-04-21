package com.renlip.fiis.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.domain.dto.FundoResponse;
import com.renlip.fiis.domain.entity.Fundo;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.MensagemEnum;
import com.renlip.fiis.domain.mapper.FundoMapper;
import com.renlip.fiis.domain.vo.FundoRequest;
import com.renlip.fiis.exception.RecursoNaoEncontradoException;
import com.renlip.fiis.exception.RegraNegocioException;
import com.renlip.fiis.repository.FundoRepository;
import com.renlip.fiis.support.UsuarioLogadoSupport;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável pelas regras de negócio relacionadas à entidade {@link Fundo}.
 *
 * <p>Orquestra as operações entre o Controller (que recebe requisições HTTP)
 * e o Repository (que acessa o banco de dados). Contém validações de domínio
 * como unicidade de ticker por usuário e conversões entre VO, Entity e DTO.</p>
 *
 * <p><b>Multi-usuário:</b> usuários com perfil {@code USER} só enxergam e
 * manipulam seus próprios fundos; {@code ADMIN} tem acesso global.</p>
 *
 * <p>Todos os métodos de escrita estão anotados com {@link Transactional}
 * para garantir atomicidade das operações no banco.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FundoService {

    private final FundoRepository fundoRepository;
    private final FundoMapper fundoMapper;
    private final UsuarioLogadoSupport usuarioLogado;

    /**
     * Lista todos os fundos visíveis ao usuário autenticado.
     * ADMIN vê todos; USER vê apenas os seus.
     */
    public List<FundoResponse> listarTodos() {
        List<Fundo> fundos = usuarioLogado.isAdmin()
            ? fundoRepository.findAll()
            : fundoRepository.findByUsuarioId(usuarioLogado.getUsuarioIdAtual());
        return fundoMapper.toResponseList(fundos);
    }

    /**
     * Lista apenas os fundos ativos (ativo = true) visíveis ao usuário autenticado.
     */
    public List<FundoResponse> listarAtivos() {
        List<Fundo> fundos = usuarioLogado.isAdmin()
            ? fundoRepository.findByAtivoTrue()
            : fundoRepository.findByUsuarioIdAndAtivoTrue(usuarioLogado.getUsuarioIdAtual());
        return fundoMapper.toResponseList(fundos);
    }

    /**
     * Busca um fundo pelo seu ID.
     *
     * @throws RecursoNaoEncontradoException se não existir OU se pertencer a outro usuário
     *         (USER não vê recursos alheios; a resposta é 404 para não vazar existência).
     */
    public FundoResponse buscarPorId(Long id) {
        Fundo fundo = obterEntidade(id);
        return fundoMapper.toResponse(fundo);
    }

    /**
     * Cadastra um novo fundo para o usuário autenticado.
     *
     * <p><b>Regra de negócio:</b> o mesmo usuário não pode ter dois fundos com o
     * mesmo ticker. Outros usuários podem ter o mesmo ticker em suas carteiras.</p>
     *
     * @throws RegraNegocioException se o usuário já tiver um fundo com o ticker informado
     */
    @Transactional
    public FundoResponse criar(FundoRequest request) {
        Usuario dono = usuarioLogado.getUsuarioAtual();

        if (fundoRepository.existsByUsuarioIdAndTicker(dono.getId(), request.ticker())) {
            throw new RegraNegocioException(MensagemEnum.FUNDO_TICKER_JA_CADASTRADO, request.ticker());
        }

        Fundo fundo = Fundo.builder()
            .usuario(dono)
            .ticker(request.ticker())
            .nome(request.nome())
            .cnpj(request.cnpj())
            .tipo(request.tipo())
            .segmento(request.segmento())
            .ativo(request.ativo() != null ? request.ativo() : true)
            .build();

        Fundo salvo = fundoRepository.save(fundo);
        return fundoMapper.toResponse(salvo);
    }

    /**
     * Atualiza um fundo existente do usuário autenticado (ou qualquer um, se ADMIN).
     *
     * <p><b>Regra de negócio:</b> se o ticker for alterado, o dono do fundo não pode
     * ter outro fundo com esse ticker. Note que a duplicidade é verificada no contexto
     * do <i>dono</i> do fundo (não do autenticado), para o caso de ADMIN editar
     * recurso alheio.</p>
     */
    @Transactional
    public FundoResponse atualizar(Long id, FundoRequest request) {
        Fundo fundo = obterEntidade(id);

        boolean tickerMudou = !fundo.getTicker().equalsIgnoreCase(request.ticker());
        if (tickerMudou
                && fundoRepository.existsByUsuarioIdAndTicker(fundo.getUsuario().getId(), request.ticker())) {
            throw new RegraNegocioException(MensagemEnum.FUNDO_TICKER_JA_CADASTRADO, request.ticker());
        }

        fundo.setTicker(request.ticker());
        fundo.setNome(request.nome());
        fundo.setCnpj(request.cnpj());
        fundo.setTipo(request.tipo());
        fundo.setSegmento(request.segmento());
        if (request.ativo() != null) {
            fundo.setAtivo(request.ativo());
        }

        Fundo atualizado = fundoRepository.save(fundo);
        return fundoMapper.toResponse(atualizado);
    }

    /**
     * Desativa um fundo (soft delete).
     */
    @Transactional
    public void desativar(Long id) {
        Fundo fundo = obterEntidade(id);
        fundo.setAtivo(false);
        fundoRepository.save(fundo);
    }

    /**
     * Busca a entidade por ID aplicando o filtro de ownership.
     *
     * <p>USER só enxerga os próprios fundos; ADMIN enxerga todos. Caso o fundo
     * não exista ou pertença a outro usuário (para USER), lança 404.</p>
     */
    private Fundo obterEntidade(Long id) {
        Optional<Fundo> fundo = usuarioLogado.isAdmin()
            ? fundoRepository.findById(id)
            : fundoRepository.findByIdAndUsuarioId(id, usuarioLogado.getUsuarioIdAtual());

        return fundo.orElseThrow(() ->
            new RecursoNaoEncontradoException(MensagemEnum.FUNDO_NAO_ENCONTRADO, id));
    }
}
