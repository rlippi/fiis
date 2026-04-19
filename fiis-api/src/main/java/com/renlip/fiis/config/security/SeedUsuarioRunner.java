package com.renlip.fiis.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.renlip.fiis.domain.entity.Usuario;
import com.renlip.fiis.domain.enumeration.Perfil;
import com.renlip.fiis.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seed do usuário administrador inicial da aplicação.
 *
 * <p>Lê as variáveis de ambiente {@code FIIS_ADMIN_EMAIL}, {@code FIIS_ADMIN_PASSWORD}
 * e (opcional) {@code FIIS_ADMIN_NOME}. Se alguma credencial estiver ausente apenas
 * loga um warning; se o e-mail já existir no banco, não faz nada. Dessa forma o
 * startup continua seguro e o seed é idempotente.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeedUsuarioRunner implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${fiis.admin.email:}")
    private String adminEmail;

    @Value("${fiis.admin.password:}")
    private String adminPassword;

    @Value("${fiis.admin.nome:Administrador}")
    private String adminNome;

    @Override
    public void run(final String... args) {
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            log.warn("Variáveis FIIS_ADMIN_EMAIL / FIIS_ADMIN_PASSWORD não definidas. Seed do administrador ignorado.");
            return;
        }

        if (usuarioRepository.existsByEmail(adminEmail)) {
            log.info("Usuário administrador '{}' já existe. Seed ignorado.", adminEmail);
            return;
        }

        Usuario admin = Usuario.builder()
            .email(adminEmail)
            .senha(passwordEncoder.encode(adminPassword))
            .nome(adminNome)
            .perfil(Perfil.ADMIN)
            .ativo(true)
            .build();

        usuarioRepository.save(admin);
        log.info("Usuário administrador '{}' criado com sucesso.", adminEmail);
    }
}
