package com.renlip.fiis.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renlip.fiis.domain.model.Fundo;
import com.renlip.fiis.domain.repository.FundoRepository;
import com.renlip.fiis.dto.FundoResponse;
import com.renlip.fiis.exception.RecursoNaoEncontradoException;
import com.renlip.fiis.exception.RegraNegocioException;
import com.renlip.fiis.vo.FundoRequest;

import lombok.RequiredArgsConstructor;

/**
 * Service responsável pelas regras de negócio relacionadas à entidade {@link Fundo}.
 *
 * <p>Orquestra as operações entre o Controller (que recebe requisições HTTP)
 * e o Repository (que acessa o banco de dados). Contém validações de domínio
 * como unicidade de ticker e conversões entre VO, Entity e DTO.</p>
 *
 * <p>Todos os métodos de escrita estão anotados com {@link Transactional}
 * para garantir atomicidade das operações no banco.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FundoService {

    private final FundoRepository fundoRepository;

    /**
     * Lista todos os fundos cadastrados no sistema.
     *
     * @return lista de fundos (pode estar vazia)
     */
    public List<FundoResponse> listarTodos() {
        return fundoRepository.findAll().stream()
            .map(FundoResponse::of)
            .toList();
    }

    /**
     * Lista apenas os fundos ativos (ativo = true).
     *
     * @return lista de fundos ativos
     */
    public List<FundoResponse> listarAtivos() {
        return fundoRepository.findByAtivoTrue().stream()
            .map(FundoResponse::of)
            .toList();
    }

    /**
     * Busca um fundo pelo seu ID.
     *
     * @param id identificador do fundo
     * @return dados do fundo encontrado
     * @throws RecursoNaoEncontradoException se não existir fundo com o ID informado
     */
    public FundoResponse buscarPorId(Long id) {
        Fundo fundo = obterEntidade(id);
        return FundoResponse.of(fundo);
    }

    /**
     * Cadastra um novo fundo.
     *
     * <p><b>Regra de negócio:</b> não pode existir outro fundo com o mesmo ticker.</p>
     *
     * @param request dados do fundo a ser criado
     * @return fundo criado (com ID gerado e datas preenchidas)
     * @throws RegraNegocioException se já existir fundo com o ticker informado
     */
    @Transactional
    public FundoResponse criar(FundoRequest request) {
        if (fundoRepository.existsByTicker(request.ticker())) {
            throw new RegraNegocioException(
                "Já existe um fundo cadastrado com o ticker " + request.ticker());
        }

        Fundo fundo = Fundo.builder()
            .ticker(request.ticker())
            .nome(request.nome())
            .cnpj(request.cnpj())
            .tipo(request.tipo())
            .segmento(request.segmento())
            .ativo(request.ativo() != null ? request.ativo() : true)
            .build();

        Fundo salvo = fundoRepository.save(fundo);
        return FundoResponse.of(salvo);
    }

    /**
     * Atualiza um fundo existente.
     *
     * <p><b>Regra de negócio:</b> se o ticker for alterado, o novo ticker não
     * pode estar em uso por outro fundo.</p>
     *
     * @param id      identificador do fundo a atualizar
     * @param request novos dados
     * @return fundo atualizado
     * @throws RecursoNaoEncontradoException se o ID não existir
     * @throws RegraNegocioException         se o novo ticker já estiver em uso
     */
    @Transactional
    public FundoResponse atualizar(Long id, FundoRequest request) {
        Fundo fundo = obterEntidade(id);

        boolean tickerMudou = !fundo.getTicker().equalsIgnoreCase(request.ticker());
        if (tickerMudou && fundoRepository.existsByTicker(request.ticker())) {
            throw new RegraNegocioException(
                "Já existe um fundo cadastrado com o ticker " + request.ticker());
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
        return FundoResponse.of(atualizado);
    }

    /**
     * Desativa um fundo (soft delete).
     *
     * <p>Marca o fundo como inativo ({@code ativo = false}) mas mantém o
     * registro no banco, preservando o histórico.</p>
     *
     * @param id identificador do fundo
     * @throws RecursoNaoEncontradoException se o ID não existir
     */
    @Transactional
    public void desativar(Long id) {
        Fundo fundo = obterEntidade(id);
        fundo.setAtivo(false);
        fundoRepository.save(fundo);
    }

    /**
     * Busca a entidade pelo ID ou lança exceção.
     * Método privado para reuso interno.
     */
    private Fundo obterEntidade(Long id) {
        return fundoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Fundo com ID " + id + " não encontrado"));
    }
}
