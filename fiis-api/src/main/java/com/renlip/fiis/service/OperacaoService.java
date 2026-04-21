package com.renlip.fiis.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.domain.dto.OperacaoResponse;
import com.renlip.fiis.domain.entity.Fundo;
import com.renlip.fiis.domain.entity.Operacao;
import com.renlip.fiis.domain.enumeration.MensagemEnum;
import com.renlip.fiis.domain.enumeration.TipoOperacao;
import com.renlip.fiis.domain.mapper.OperacaoMapper;
import com.renlip.fiis.domain.vo.OperacaoRequest;
import com.renlip.fiis.exception.RecursoNaoEncontradoException;
import com.renlip.fiis.exception.RegraNegocioException;
import com.renlip.fiis.repository.FundoRepository;
import com.renlip.fiis.repository.OperacaoRepository;
import com.renlip.fiis.support.UsuarioLogadoSupport;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável pelas regras de negócio de {@link Operacao}.
 *
 * <p>Principais validações:
 * <ul>
 *   <li>Fundo referenciado deve pertencer ao usuário autenticado;</li>
 *   <li>Em uma VENDA, deve haver cotas suficientes em carteira
 *       (soma de compras − vendas).</li>
 * </ul>
 * </p>
 *
 * <p><b>Multi-usuário:</b> USER vê/edita apenas suas operações; ADMIN tem acesso global.
 * O campo {@code usuario} é sempre o dono do fundo — garante coerência mesmo quando
 * ADMIN manipula recursos alheios.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperacaoService {

    private final OperacaoRepository operacaoRepository;
    private final FundoRepository fundoRepository;
    private final OperacaoMapper operacaoMapper;
    private final UsuarioLogadoSupport usuarioLogado;

    public List<OperacaoResponse> listarTodas() {
        List<Operacao> operacoes = usuarioLogado.isAdmin()
            ? operacaoRepository.findAll()
            : operacaoRepository.findByUsuarioId(usuarioLogado.getUsuarioIdAtual());
        return operacaoMapper.toResponseList(operacoes);
    }

    /**
     * Lista as operações de um fundo específico (mais recente primeiro).
     * O fundo precisa pertencer ao usuário autenticado (ou qualquer um, se ADMIN).
     */
    public List<OperacaoResponse> listarPorFundo(Long fundoId) {
        obterFundoDoUsuario(fundoId);
        return operacaoMapper.toResponseList(operacaoRepository.findByFundoIdOrderByDataOperacaoDesc(fundoId));
    }

    public OperacaoResponse buscarPorId(Long id) {
        return operacaoMapper.toResponse(obterEntidade(id));
    }

    /**
     * Cria uma nova operação.
     *
     * <p><b>Regras:</b>
     * <ul>
     *   <li>Fundo deve existir e pertencer ao usuário autenticado (ou ADMIN pode usar qualquer);</li>
     *   <li>Para VENDA, a quantidade não pode exceder as cotas em carteira.</li>
     * </ul>
     * </p>
     *
     * <p>O {@code usuario} da operação é sempre o dono do fundo.</p>
     */
    @Transactional
    public OperacaoResponse criar(OperacaoRequest request) {
        Fundo fundo = obterFundoDoUsuario(request.fundoId());

        if (request.tipo() == TipoOperacao.VENDA) {
            Integer posicaoAtual = operacaoRepository.calcularPosicaoAtual(fundo.getId());
            if (request.quantidade() > posicaoAtual) {
                throw new RegraNegocioException(
                    MensagemEnum.OPERACAO_VENDA_EXCEDE_POSICAO, request.quantidade(), posicaoAtual);
            }
        }

        Operacao operacao = Operacao.builder()
            .usuario(fundo.getUsuario())
            .fundo(fundo)
            .tipo(request.tipo())
            .dataOperacao(request.dataOperacao())
            .quantidade(request.quantidade())
            .precoUnitario(request.precoUnitario())
            .taxas(request.taxas() != null ? request.taxas() : BigDecimal.ZERO)
            .observacao(request.observacao())
            .build();

        Operacao salva = operacaoRepository.save(operacao);
        return operacaoMapper.toResponse(salva);
    }

    @Transactional
    public OperacaoResponse atualizar(Long id, OperacaoRequest request) {
        Operacao operacao = obterEntidade(id);
        Fundo novoFundo = obterFundoDoUsuario(request.fundoId());

        validarPosicaoParaEdicao(operacao, novoFundo.getId(), request);

        operacao.setUsuario(novoFundo.getUsuario());
        operacao.setFundo(novoFundo);
        operacao.setTipo(request.tipo());
        operacao.setDataOperacao(request.dataOperacao());
        operacao.setQuantidade(request.quantidade());
        operacao.setPrecoUnitario(request.precoUnitario());
        operacao.setTaxas(request.taxas() != null ? request.taxas() : BigDecimal.ZERO);
        operacao.setObservacao(request.observacao());

        Operacao atualizada = operacaoRepository.save(operacao);
        return operacaoMapper.toResponse(atualizada);
    }

    @Transactional
    public void deletar(Long id) {
        Operacao operacao = obterEntidade(id);
        operacaoRepository.delete(operacao);
    }

    private void validarPosicaoParaEdicao(Operacao atual, Long novoFundoId, OperacaoRequest req) {
        Long fundoAtualId = atual.getFundo().getId();

        int posicaoSemEssaOp = fundoAtualId.equals(novoFundoId)
            ? operacaoRepository.calcularPosicaoExcluindo(novoFundoId, atual.getId())
            : operacaoRepository.calcularPosicaoAtual(novoFundoId);

        int efeitoNovo = req.tipo() == TipoOperacao.COMPRA ? req.quantidade() : -req.quantidade();
        int posicaoFinal = posicaoSemEssaOp + efeitoNovo;

        if (posicaoFinal < 0) {
            throw new RegraNegocioException(
                MensagemEnum.OPERACAO_EDICAO_POSICAO_NEGATIVA, posicaoSemEssaOp, efeitoNovo);
        }
    }

    private Operacao obterEntidade(Long id) {
        Optional<Operacao> operacao = usuarioLogado.isAdmin()
            ? operacaoRepository.findById(id)
            : operacaoRepository.findByIdAndUsuarioId(id, usuarioLogado.getUsuarioIdAtual());

        return operacao.orElseThrow(() ->
            new RecursoNaoEncontradoException(MensagemEnum.OPERACAO_NAO_ENCONTRADA, id));
    }

    private Fundo obterFundoDoUsuario(Long fundoId) {
        Optional<Fundo> fundo = usuarioLogado.isAdmin()
            ? fundoRepository.findById(fundoId)
            : fundoRepository.findByIdAndUsuarioId(fundoId, usuarioLogado.getUsuarioIdAtual());

        return fundo.orElseThrow(() ->
            new RecursoNaoEncontradoException(MensagemEnum.FUNDO_NAO_ENCONTRADO, fundoId));
    }
}
