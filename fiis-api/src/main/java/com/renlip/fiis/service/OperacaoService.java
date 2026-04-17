package com.renlip.fiis.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.domain.enums.TipoOperacao;
import com.renlip.fiis.domain.model.Fundo;
import com.renlip.fiis.domain.model.Operacao;
import com.renlip.fiis.domain.repository.FundoRepository;
import com.renlip.fiis.domain.repository.OperacaoRepository;
import com.renlip.fiis.dto.OperacaoResponse;
import com.renlip.fiis.exception.RecursoNaoEncontradoException;
import com.renlip.fiis.exception.RegraNegocioException;
import com.renlip.fiis.vo.OperacaoRequest;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável pelas regras de negócio de {@link Operacao}.
 *
 * <p>Principais validações:
 * <ul>
 *   <li>Fundo referenciado deve existir;</li>
 *   <li>Em uma VENDA, deve haver cotas suficientes em carteira
 *       (soma de compras − vendas).</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperacaoService {

    private final OperacaoRepository operacaoRepository;
    private final FundoRepository fundoRepository;

    /**
     * Lista todas as operações cadastradas.
     *
     * @return lista completa de operações
     */
    public List<OperacaoResponse> listarTodas() {
        return operacaoRepository.findAll().stream()
            .map(OperacaoResponse::of)
            .toList();
    }

    /**
     * Lista as operações de um fundo específico, ordenadas da mais recente
     * para a mais antiga.
     *
     * @param fundoId ID do fundo
     * @return lista de operações
     * @throws RecursoNaoEncontradoException se o fundo não existir
     */
    public List<OperacaoResponse> listarPorFundo(Long fundoId) {
        validarFundoExiste(fundoId);
        return operacaoRepository.findByFundoIdOrderByDataOperacaoDesc(fundoId).stream()
            .map(OperacaoResponse::of)
            .toList();
    }

    /**
     * Busca uma operação pelo ID.
     *
     * @param id identificador
     * @return operação encontrada
     * @throws RecursoNaoEncontradoException se não existir
     */
    public OperacaoResponse buscarPorId(Long id) {
        return OperacaoResponse.of(obterEntidade(id));
    }

    /**
     * Cria uma nova operação.
     *
     * <p><b>Regras:</b>
     * <ul>
     *   <li>Fundo deve existir;</li>
     *   <li>Para VENDA, a quantidade não pode exceder as cotas em carteira.</li>
     * </ul>
     * </p>
     *
     * @param request dados da nova operação
     * @return operação criada
     */
    @Transactional
    public OperacaoResponse criar(OperacaoRequest request) {
        Fundo fundo = obterFundo(request.fundoId());

        if (request.tipo() == TipoOperacao.VENDA) {
            Integer posicaoAtual = operacaoRepository.calcularPosicaoAtual(fundo.getId());
            if (request.quantidade() > posicaoAtual) {
                throw new RegraNegocioException(
                    "Quantidade a vender (" + request.quantidade() +
                    ") excede a posição atual em carteira (" + posicaoAtual + ")");
            }
        }

        Operacao operacao = Operacao.builder()
            .fundo(fundo)
            .tipo(request.tipo())
            .dataOperacao(request.dataOperacao())
            .quantidade(request.quantidade())
            .precoUnitario(request.precoUnitario())
            .taxas(request.taxas() != null ? request.taxas() : BigDecimal.ZERO)
            .observacao(request.observacao())
            .build();

        Operacao salva = operacaoRepository.save(operacao);
        return OperacaoResponse.of(salva);
    }

    /**
     * Atualiza uma operação existente.
     *
     * <p>Permite trocar de fundo, tipo, quantidade, preço, data, taxas e
     * observação. Revalida as regras de venda considerando a nova situação.</p>
     *
     * @param id      identificador da operação
     * @param request novos dados
     * @return operação atualizada
     * @throws RecursoNaoEncontradoException se a operação ou o fundo não existirem
     * @throws RegraNegocioException         se a nova venda deixar posição negativa
     */
    @Transactional
    public OperacaoResponse atualizar(Long id, OperacaoRequest request) {
        Operacao operacao = obterEntidade(id);
        Fundo novoFundo = obterFundo(request.fundoId());

        validarPosicaoParaEdicao(operacao, novoFundo.getId(), request);

        operacao.setFundo(novoFundo);
        operacao.setTipo(request.tipo());
        operacao.setDataOperacao(request.dataOperacao());
        operacao.setQuantidade(request.quantidade());
        operacao.setPrecoUnitario(request.precoUnitario());
        operacao.setTaxas(request.taxas() != null ? request.taxas() : BigDecimal.ZERO);
        operacao.setObservacao(request.observacao());

        Operacao atualizada = operacaoRepository.save(operacao);
        return OperacaoResponse.of(atualizada);
    }

    /**
     * Remove uma operação do banco (hard delete).
     *
     * <p>Diferente do {@code Fundo} (que usa soft delete), operações são
     * apagadas mesmo. Se precisar de histórico, pode-se migrar futuramente.</p>
     *
     * @param id identificador
     * @throws RecursoNaoEncontradoException se não existir
     */
    @Transactional
    public void deletar(Long id) {
        Operacao operacao = obterEntidade(id);
        operacaoRepository.delete(operacao);
    }

    /**
     * Valida se a alteração mantém a posição consistente (não negativa).
     *
     * <p>Recalcula a posição do fundo ignorando o efeito da operação atual
     * e simula o impacto da operação no formato editado.</p>
     */
    private void validarPosicaoParaEdicao(Operacao atual, Long novoFundoId, OperacaoRequest req) {
        Long fundoAtualId = atual.getFundo().getId();

        int posicaoSemEssaOp = fundoAtualId.equals(novoFundoId)
            ? operacaoRepository.calcularPosicaoExcluindo(novoFundoId, atual.getId())
            : operacaoRepository.calcularPosicaoAtual(novoFundoId);

        int efeitoNovo = req.tipo() == TipoOperacao.COMPRA ? req.quantidade() : -req.quantidade();
        int posicaoFinal = posicaoSemEssaOp + efeitoNovo;

        if (posicaoFinal < 0) {
            throw new RegraNegocioException(
                "Edição inválida: resultaria em posição negativa no fundo. " +
                "Posição sem essa operação: " + posicaoSemEssaOp +
                ", efeito da nova operação: " + efeitoNovo);
        }
    }

    private Operacao obterEntidade(Long id) {
        return operacaoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Operação com ID " + id + " não encontrada"));
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
