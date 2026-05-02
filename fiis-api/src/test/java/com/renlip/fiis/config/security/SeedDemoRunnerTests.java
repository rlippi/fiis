package com.renlip.fiis.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.renlip.fiis.config.SeedDemoProperties;
import com.renlip.fiis.domain.entity.Cotacao;
import com.renlip.fiis.domain.entity.Fundo;
import com.renlip.fiis.domain.entity.Operacao;
import com.renlip.fiis.domain.entity.Provento;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.Perfil;
import com.renlip.fiis.repository.CotacaoRepository;
import com.renlip.fiis.repository.FundoRepository;
import com.renlip.fiis.repository.OperacaoRepository;
import com.renlip.fiis.repository.ProventoRepository;
import com.renlip.fiis.repository.UsuarioRepository;

/**
 * Testes unitários do {@link SeedDemoRunner} com mocks puros.
 *
 * <p>Cobre os 3 caminhos do {@code run()}:
 * <ul>
 *   <li>flag desligada → não toca em repositório nenhum;</li>
 *   <li>flag ligada e demo inexistente → cria usuário, fundos, operações,
 *       proventos e cotações;</li>
 *   <li>flag ligada e demo já existente → sai sem recriar (idempotente).</li>
 * </ul>
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class SeedDemoRunnerTests {

    @Mock
    private SeedDemoSupport support;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FundoRepository fundoRepository;

    @Mock
    private OperacaoRepository operacaoRepository;

    @Mock
    private ProventoRepository proventoRepository;

    @Mock
    private CotacaoRepository cotacaoRepository;

    private SeedDemoRunner runner;

    @BeforeEach
    void setupRunner() {
        // SeedDemoProperties é criado em cada teste para variar o flag/credenciais
        // sem precisar de @Mock para um record imutável.
    }

    private void instanciarRunnerCom(SeedDemoProperties props) {
        runner = new SeedDemoRunner(
            props,
            support,
            passwordEncoder,
            usuarioRepository,
            fundoRepository,
            operacaoRepository,
            proventoRepository,
            cotacaoRepository
        );
    }

    @Nested
    @DisplayName("Flag desligada")
    class FlagDesligada {

        @Test
        @DisplayName("Não toca em nenhum repositório quando enabled=false")
        void testFlagOffNaoFazNada() {
            instanciarRunnerCom(new SeedDemoProperties(false, "demo@fiis.com", "demo1234", "Demo"));

            runner.run();

            verify(usuarioRepository, never()).existsByEmail(anyString());
            verify(usuarioRepository, never()).save(any());
            verify(fundoRepository, never()).saveAll(any());
            verify(operacaoRepository, never()).saveAll(any());
            verify(proventoRepository, never()).saveAll(any());
            verify(cotacaoRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("Flag ligada — primeira execução")
    class PrimeiraExecucao {

        @Test
        @DisplayName("Cria usuário (USER + ativo + senha codificada) e popula carteira completa")
        void testCriaUsuarioEPopulaCarteira() {
            SeedDemoProperties props = new SeedDemoProperties(true, "demo@fiis.com", "demo1234", "Conta Demo");
            instanciarRunnerCom(props);

            when(usuarioRepository.existsByEmail("demo@fiis.com")).thenReturn(false);
            when(passwordEncoder.encode("demo1234")).thenReturn("$2a$10$hash-codificado");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                u.setId(1L);
                return u;
            });

            List<Fundo> fundosFakes = List.of(new Fundo(), new Fundo());
            when(support.criarFundos(any())).thenReturn(fundosFakes);
            when(fundoRepository.saveAll(anyList())).thenReturn(fundosFakes);
            when(support.criarOperacoes(any(), any())).thenReturn(List.<Operacao>of());
            when(support.criarProventos(any(), any())).thenReturn(List.<Provento>of());
            when(support.criarCotacoes(any(), any())).thenReturn(List.<Cotacao>of());

            runner.run();

            ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository, times(1)).save(usuarioCaptor.capture());
            Usuario salvo = usuarioCaptor.getValue();
            assertThat(salvo.getEmail()).isEqualTo("demo@fiis.com");
            assertThat(salvo.getNome()).isEqualTo("Conta Demo");
            assertThat(salvo.getSenha()).isEqualTo("$2a$10$hash-codificado");
            assertThat(salvo.getPerfil()).isEqualTo(Perfil.USER);
            assertThat(salvo.getAtivo()).isTrue();

            verify(fundoRepository, times(1)).saveAll(fundosFakes);
            verify(operacaoRepository, times(1)).saveAll(any());
            verify(proventoRepository, times(1)).saveAll(any());
            verify(cotacaoRepository, times(1)).saveAll(any());
        }

        @Test
        @DisplayName("Aborta sem mexer em nada quando flag ligada mas email/senha vazios")
        void testFlagOnComCredenciaisVaziasAborta() {
            instanciarRunnerCom(new SeedDemoProperties(true, "", "", "Demo"));

            runner.run();

            verify(usuarioRepository, never()).existsByEmail(anyString());
            verify(usuarioRepository, never()).save(any());
            verify(fundoRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("Flag ligada — segunda execução (idempotência)")
    class SegundaExecucao {

        @Test
        @DisplayName("Não recria nem duplica quando o usuário demo já existe")
        void testIdempotenciaQuandoDemoJaExiste() {
            SeedDemoProperties props = new SeedDemoProperties(true, "demo@fiis.com", "demo1234", "Demo");
            instanciarRunnerCom(props);

            when(usuarioRepository.existsByEmail("demo@fiis.com")).thenReturn(true);

            runner.run();

            verify(usuarioRepository, times(1)).existsByEmail("demo@fiis.com");
            verify(usuarioRepository, never()).save(any());
            verify(fundoRepository, never()).saveAll(any());
            verify(operacaoRepository, never()).saveAll(any());
            verify(proventoRepository, never()).saveAll(any());
            verify(cotacaoRepository, never()).saveAll(any());
        }
    }
}
