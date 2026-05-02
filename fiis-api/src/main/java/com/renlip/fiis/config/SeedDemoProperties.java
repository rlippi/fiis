package com.renlip.fiis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurações do seed de dados demo.
 *
 * <p>Quando {@code enabled=true}, o {@code SeedDemoRunner} cria, no startup,
 * um usuário com carteira de exemplo para visitantes explorarem o app sem
 * precisar se cadastrar. Mantém-se desligado em dev/test (default) para
 * não poluir o banco local nem os testes — em HML a flag é ligada via
 * {@code FIIS_SEED_DEMO_ENABLED=true}.</p>
 *
 * @param enabled liga/desliga a criação do usuário e da carteira demo
 * @param email   e-mail do usuário demo (público — visitante usa para entrar)
 * @param senha   senha em texto plano (será codificada via BCrypt antes do save)
 * @param nome    nome exibido na tela do usuário demo
 */
@ConfigurationProperties(prefix = "fiis.seed.demo")
public record SeedDemoProperties(
    boolean enabled,
    String email,
    String senha,
    String nome
) {
}
