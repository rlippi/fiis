package com.renlip.fiis.config.security;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.renlip.fiis.config.SeedDemoProperties;
import com.renlip.fiis.domain.entity.Fundo;
import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.Perfil;
import com.renlip.fiis.repository.CotacaoRepository;
import com.renlip.fiis.repository.FundoRepository;
import com.renlip.fiis.repository.OperacaoRepository;
import com.renlip.fiis.repository.ProventoRepository;
import com.renlip.fiis.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cria, no startup, um usuário demo público com carteira de exemplo para
 * que visitantes consigam explorar o app sem se cadastrar.
 *
 * <p>Idempotente em duas camadas:
 * <ul>
 *   <li>se {@code fiis.seed.demo.enabled=false} (default), sai sem fazer nada;</li>
 *   <li>se o e-mail já existe, sai sem fazer nada — preserva customizações
 *       que o visitante eventualmente tenha feito na própria conta demo.</li>
 * </ul>
 * Sob {@code @Transactional}, a criação inteira (usuário + fundos + operações
 * + proventos + cotações) é atômica — se algo no meio falhar, nada persiste.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeedDemoRunner implements CommandLineRunner {

    private final SeedDemoProperties properties;
    private final SeedDemoSupport support;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final FundoRepository fundoRepository;
    private final OperacaoRepository operacaoRepository;
    private final ProventoRepository proventoRepository;
    private final CotacaoRepository cotacaoRepository;

    @Override
    @Transactional
    public void run(final String... args) {
        if (!properties.enabled()) {
            log.info("Seed demo desligado (fiis.seed.demo.enabled=false). Skip.");
            return;
        }

        if (!StringUtils.hasText(properties.email()) || !StringUtils.hasText(properties.senha())) {
            log.warn("fiis.seed.demo.enabled=true mas email/senha vazios. Seed demo abortado.");
            return;
        }

        if (usuarioRepository.existsByEmail(properties.email())) {
            log.info("Usuário demo '{}' já existe. Seed ignorado.", properties.email());
            return;
        }

        Usuario demo = usuarioRepository.save(Usuario.builder()
            .email(properties.email())
            .senha(passwordEncoder.encode(properties.senha()))
            .nome(properties.nome())
            .perfil(Perfil.USER)
            .ativo(true)
            .build());

        List<Fundo> fundos = fundoRepository.saveAll(support.criarFundos(demo));
        operacaoRepository.saveAll(support.criarOperacoes(demo, fundos));
        proventoRepository.saveAll(support.criarProventos(demo, fundos));
        cotacaoRepository.saveAll(support.criarCotacoes(demo, fundos));

        log.info("Seed demo criado: usuário='{}', fundos={}.", properties.email(), fundos.size());
    }
}
