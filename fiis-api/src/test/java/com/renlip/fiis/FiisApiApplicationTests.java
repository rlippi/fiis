package com.renlip.fiis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Sanity check do contexto Spring.
 *
 * <p>Se este teste passa, significa que:
 * <ul>
 *   <li>O banco {@code fiis_test} no Postgres local está acessível;</li>
 *   <li>O Hibernate conseguiu conectar e criar o schema;</li>
 *   <li>Todos os beans da aplicação inicializaram sem erro.</li>
 * </ul>
 * </p>
 */
@ActiveProfiles("test")
@SpringBootTest(classes = FiisApiApplication.class)
class FiisApiApplicationTests {

    @Test
    void contextLoads() {
        // Sucesso = contexto subiu. Não precisa de assertion explícita.
    }
}
