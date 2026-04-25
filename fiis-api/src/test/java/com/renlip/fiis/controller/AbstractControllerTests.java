package com.renlip.fiis.controller;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.renlip.fiis.FiisApiApplication;
import com.renlip.fiis.util.RestTestClient;

/**
 * Classe base dos testes de integração de Controllers.
 *
 * <p>Configura:
 * <ul>
 *   <li><b>Profile "test"</b> ativo — carrega {@code application-test.properties}
 *       apontando para o banco {@code fiis_test} no Postgres local;</li>
 *   <li><b>MockMvc</b> autowired (via {@link AutoConfigureMockMvc});</li>
 *   <li><b>{@link RestTestClient}</b> instanciado antes de cada teste.</li>
 * </ul>
 * </p>
 *
 * <p>Expõe o método {@link #executeSqlScript(String)} para testes que
 * precisem rodar scripts SQL complementares em runtime.</p>
 */
@ActiveProfiles("test")
@SpringBootTest(classes = FiisApiApplication.class)
@AutoConfigureMockMvc
@WithUserDetails("test@fiis.com")
public abstract class AbstractControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CacheManager cacheManager;

    /**
     * Cliente HTTP fluente pronto para uso em cada teste.
     * Inicializado em {@link #setUpRestTestClient()}.
     */
    protected RestTestClient restTestClient;

    /**
     * Instancia o {@link RestTestClient} e zera todos os caches Spring antes
     * de cada {@code @Test}.
     *
     * <p>Limpar o cache é necessário porque o contexto Spring é compartilhado
     * entre os testes (cache de contexto do Spring Test): sem o reset, dados
     * cacheados em um teste anterior (ex: última cotação do fundo {@code id=1})
     * sobrevivem ao TRUNCATE da fixture {@code setup.sql} e contaminam o
     * teste seguinte. As entidades JPA são recriadas com mesmos IDs após
     * {@code TRUNCATE ... RESTART IDENTITY}, então a colisão é determinística.</p>
     */
    @BeforeEach
    void setUpRestTestClient() {
        this.restTestClient = new RestTestClient(mockMvc);
        for (String name : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    /**
     * Executa um script SQL do classpath na mesma conexão do DataSource atual.
     *
     * <p>Útil quando o teste precisa carregar dados específicos além do
     * {@code setup.sql} e do {@code fii-script.sql} já declarados em
     * {@code @Sql} na classe.</p>
     *
     * @param classpathPath caminho relativo dentro de {@code src/test/resources/}
     * @throws SQLException se a conexão ou a execução falharem
     */
    protected void executeSqlScript(String classpathPath) throws SQLException {
        if (classpathPath == null || classpathPath.isBlank()) {
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(classpathPath));
        }
    }
}
