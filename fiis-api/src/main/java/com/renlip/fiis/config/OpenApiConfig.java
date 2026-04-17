package com.renlip.fiis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Configuração do Swagger / OpenAPI.
 *
 * <p>Define os metadados (título, versão, contato, licença) que aparecem
 * no topo da interface do Swagger UI.</p>
 *
 * <p>Acesso:
 * <ul>
 *   <li>Swagger UI: <a href="http://localhost:8081/swagger-ui/index.html">http://localhost:8081/swagger-ui/index.html</a></li>
 *   <li>OpenAPI JSON: <a href="http://localhost:8081/v3/api-docs">http://localhost:8081/v3/api-docs</a></li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Cria o bean {@link OpenAPI} com os metadados do projeto.
     *
     * @return configuração customizada do OpenAPI
     */
    @Bean
    public OpenAPI fiisOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("FIIs API")
                .description("API do gerenciador de carteira de Fundos de Investimento Imobiliário")
                .version("v0.0.1")
                .contact(new Contact()
                    .name("Renato")
                    .email("rlippi@hotmail.com")
                    .url("https://github.com/rlippi/fiis"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")));
    }
}
