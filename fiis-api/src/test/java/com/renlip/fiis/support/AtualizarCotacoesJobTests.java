package com.renlip.fiis.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.renlip.fiis.config.JobProperties;
import com.renlip.fiis.config.JobProperties.AtualizarCotacoes;
import com.renlip.fiis.domain.dto.ImportacaoBrapiResponse;
import com.renlip.fiis.domain.dto.ResumoJobResponse;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.Perfil;
import com.renlip.fiis.repository.UsuarioRepository;
import com.renlip.fiis.service.CotacaoService;

/**
 * Testes unitários do {@link AtualizarCotacoesJob} com mocks puros.
 *
 * <p>Separado dos testes de integração porque aqui o foco é o comportamento
 * interno do orquestrador: respeito à flag {@code enabled}, isolamento de
 * falhas entre usuários e consolidação dos totais. Sem Spring no meio, os
 * cenários rodam em milissegundos.</p>
 */
@ExtendWith(MockitoExtension.class)
class AtualizarCotacoesJobTests {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CotacaoService cotacaoService;

    @Mock
    private JobProperties jobProperties;

    @InjectMocks
    private AtualizarCotacoesJob job;

    private Usuario usuario(long id, String email) {
        return Usuario.builder()
            .id(id)
            .email(email)
            .nome("Usuário " + id)
            .perfil(Perfil.USER)
            .ativo(true)
            .build();
    }

    @Nested
    @DisplayName("agendar()")
    class Agendar {

        @Test
        @DisplayName("Não executa quando fiis.job.atualizar-cotacoes.enabled = false")
        void testAgendarNaoExecutaQuandoDesabilitado() {
            when(jobProperties.atualizarCotacoes()).thenReturn(new AtualizarCotacoes(false, "0 0 19 * * MON-FRI"));

            job.agendar();

            verify(usuarioRepository, never()).findByAtivoTrue();
            verify(cotacaoService, never()).importarViaBrapiPara(any());
        }

        @Test
        @DisplayName("Executa normalmente quando enabled = true")
        void testAgendarExecutaQuandoHabilitado() {
            when(jobProperties.atualizarCotacoes()).thenReturn(new AtualizarCotacoes(true, "0 0 19 * * MON-FRI"));
            when(usuarioRepository.findByAtivoTrue()).thenReturn(List.of());

            job.agendar();

            verify(usuarioRepository, times(1)).findByAtivoTrue();
        }
    }

    @Nested
    @DisplayName("executar()")
    class Executar {

        @Test
        @DisplayName("Sem usuários ativos retorna zeros e não chama o service")
        void testExecutarSemUsuarios() {
            when(usuarioRepository.findByAtivoTrue()).thenReturn(List.of());

            ResumoJobResponse resumo = job.executar();

            assertThat(resumo.usuariosProcessados()).isZero();
            assertThat(resumo.comSucesso()).isZero();
            assertThat(resumo.comFalha()).isZero();
            assertThat(resumo.cotacoesCriadas()).isZero();
            assertThat(resumo.cotacoesAtualizadas()).isZero();
            verify(cotacaoService, never()).importarViaBrapiPara(any());
        }

        @Test
        @DisplayName("Consolida sucessos de múltiplos usuários")
        void testExecutarConsolidaSucessos() {
            Usuario u1 = usuario(1L, "u1@fiis.com");
            Usuario u2 = usuario(2L, "u2@fiis.com");
            when(usuarioRepository.findByAtivoTrue()).thenReturn(List.of(u1, u2));
            when(cotacaoService.importarViaBrapiPara(u1))
                .thenReturn(new ImportacaoBrapiResponse(3, 2, 1, List.of()));
            when(cotacaoService.importarViaBrapiPara(u2))
                .thenReturn(new ImportacaoBrapiResponse(2, 1, 1, List.of("XPTO11")));

            ResumoJobResponse resumo = job.executar();

            assertThat(resumo.usuariosProcessados()).isEqualTo(2);
            assertThat(resumo.comSucesso()).isEqualTo(2);
            assertThat(resumo.comFalha()).isZero();
            assertThat(resumo.cotacoesCriadas()).isEqualTo(3);
            assertThat(resumo.cotacoesAtualizadas()).isEqualTo(2);
        }

        @Test
        @DisplayName("Isola falhas — exceção em 1 usuário não interrompe os demais")
        void testExecutarIsolaFalhas() {
            Usuario u1 = usuario(1L, "u1@fiis.com");
            Usuario u2 = usuario(2L, "u2@fiis.com");
            Usuario u3 = usuario(3L, "u3@fiis.com");
            when(usuarioRepository.findByAtivoTrue()).thenReturn(List.of(u1, u2, u3));
            when(cotacaoService.importarViaBrapiPara(u1))
                .thenReturn(new ImportacaoBrapiResponse(1, 1, 0, List.of()));
            when(cotacaoService.importarViaBrapiPara(u2))
                .thenThrow(new RuntimeException("BRAPI fora do ar"));
            when(cotacaoService.importarViaBrapiPara(u3))
                .thenReturn(new ImportacaoBrapiResponse(1, 0, 1, List.of()));

            ResumoJobResponse resumo = job.executar();

            assertThat(resumo.usuariosProcessados()).isEqualTo(3);
            assertThat(resumo.comSucesso()).isEqualTo(2);
            assertThat(resumo.comFalha()).isEqualTo(1);
            assertThat(resumo.cotacoesCriadas()).isEqualTo(1);
            assertThat(resumo.cotacoesAtualizadas()).isEqualTo(1);
            verify(cotacaoService, times(3)).importarViaBrapiPara(any());
        }
    }
}
