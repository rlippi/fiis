package com.renlip.fiis.support;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.renlip.fiis.config.JobProperties;
import com.renlip.fiis.domain.dto.ImportacaoBrapiResponse;
import com.renlip.fiis.domain.dto.ResumoJobResponse;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.repository.UsuarioRepository;
import com.renlip.fiis.service.CotacaoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Job de atualização diária de cotações via BRAPI para todos os usuários ativos.
 *
 * <p><b>Por que fora de um Service:</b> o service {@link CotacaoService} opera
 * no contexto de um único usuário (autenticado via HTTP ou passado como
 * parâmetro). Este componente orquestra a iteração sobre todos os usuários e
 * consolida o resumo — papel de "job", não de "regra de negócio".</p>
 *
 * <p><b>Transações por usuário:</b> cada chamada a
 * {@link CotacaoService#importarViaBrapiPara(Usuario)} passa pelo proxy do Spring
 * e abre sua própria transação. Assim, uma falha em um usuário (BRAPI fora do ar,
 * carteira vazia, erro de banco) não faz rollback das atualizações dos usuários
 * anteriores — cada usuário é isolado.</p>
 *
 * <p><b>Dois pontos de entrada:</b>
 * <ul>
 *   <li>{@link #agendar()} — invocado automaticamente pelo Spring no horário
 *       configurado em {@code fiis.job.atualizar-cotacoes.cron}. Respeita a flag
 *       {@code enabled} para permitir desligar sem redeploy.</li>
 *   <li>{@link #executar()} — invocável diretamente via endpoint admin
 *       ({@code POST /api/jobs/atualizar-cotacoes}) ou por testes unitários.
 *       Roda mesmo com {@code enabled = false}.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AtualizarCotacoesJob {

    private final UsuarioRepository usuarioRepository;

    private final CotacaoService cotacaoService;

    private final JobProperties jobProperties;

    /**
     * Ponto de entrada do agendador do Spring. A expressão cron é lida do YAML
     * no startup — alterar em runtime exige reinicialização.
     *
     * <p>Respeita a flag {@code fiis.job.atualizar-cotacoes.enabled}: quando
     * {@code false}, a execução é pulada silenciosamente (útil para pausar
     * temporariamente via variável de ambiente no Render, sem redeploy).</p>
     */
    @Scheduled(cron = "${fiis.job.atualizar-cotacoes.cron}")
    public void agendar() {
        if (!jobProperties.atualizarCotacoes().enabled()) {
            log.debug("[JOB atualizar-cotacoes] Desabilitado via fiis.job.atualizar-cotacoes.enabled=false. Ignorando execução.");
            return;
        }
        executar();
    }

    /**
     * Executa o job ignorando a flag {@code enabled}. Disponível para disparo
     * manual via endpoint admin e para testes.
     *
     * <p>Itera sobre todos os usuários ativos. Cada usuário é processado em uma
     * transação independente — falhas são capturadas, logadas e contabilizadas
     * em {@code comFalha}, sem interromper o restante da execução.</p>
     *
     * @return resumo consolidado da execução
     */
    public ResumoJobResponse executar() {
        List<Usuario> usuarios = usuarioRepository.findByAtivoTrue();
        log.info("[JOB atualizar-cotacoes] Início — {} usuário(s) ativo(s)", usuarios.size());

        int comSucesso = 0;
        int comFalha = 0;
        int criadosTotal = 0;
        int atualizadosTotal = 0;

        for (Usuario usuario : usuarios) {
            try {
                ImportacaoBrapiResponse resumo = cotacaoService.importarViaBrapiPara(usuario);
                log.info("[JOB atualizar-cotacoes] Usuário {} ({}): criados={}, atualizados={}, naoEncontrados={}",
                    usuario.getId(), usuario.getEmail(),
                    resumo.criados(), resumo.atualizados(), resumo.naoEncontradosBrapi().size());
                comSucesso++;
                criadosTotal += resumo.criados();
                atualizadosTotal += resumo.atualizados();
            } catch (Exception ex) {
                log.warn("[JOB atualizar-cotacoes] Falha ao atualizar usuário {} ({}): {}",
                    usuario.getId(), usuario.getEmail(), ex.getMessage());
                comFalha++;
            }
        }

        log.info("[JOB atualizar-cotacoes] Fim — sucesso={} falha={} criados={} atualizados={}",
            comSucesso, comFalha, criadosTotal, atualizadosTotal);

        return new ResumoJobResponse(usuarios.size(), comSucesso, comFalha, criadosTotal, atualizadosTotal);
    }
}
