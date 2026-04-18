package com.renlip.fiis.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * Configuração do Swagger / OpenAPI.
 *
 * <p>Define os metadados (título, versão, contato, licença) que aparecem
 * no topo da interface do Swagger UI e registra o esquema de segurança JWT
 * Bearer — expondo o botão <b>Authorize</b> para autenticar via token antes
 * das chamadas.</p>
 *
 * <p>Acesso:
 * <ul>
 *   <li>Swagger UI: <a href="http://localhost:8081/swagger-ui/index.html">http://localhost:8081/swagger-ui/index.html</a></li>
 *   <li>OpenAPI JSON: <a href="http://localhost:8081/v3/api-docs">http://localhost:8081/v3/api-docs</a></li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    /**
     * Cria o bean {@link OpenAPI} com os metadados do projeto e registra o
     * esquema de segurança Bearer usado por todas as rotas protegidas.
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
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
            .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                new SecurityScheme()
                    .name(BEARER_SCHEME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Cole apenas o token JWT (sem o prefixo 'Bearer ').")))
            .tags(List.of(
                new Tag().name("Autenticação").description("Login e geração de tokens JWT"),
                new Tag().name("Fundos").description("Operações de CRUD de Fundos de Investimento Imobiliário"),
                new Tag().name("Cotações").description("Histórico de cotações (preços de mercado) dos FIIs"),
                new Tag().name("Operações").description("Operações de compra e venda de cotas de FIIs"),
                new Tag().name("Proventos").description("Rendimentos e amortizações pagos pelos FIIs"),
                new Tag().name("Eventos Corporativos").description("Bonificações, desdobramentos e grupamentos dos FIIs"),
                new Tag().name("Posições").description("Posição consolidada em cada fundo (qtd, PM, proventos, yield)"),
                new Tag().name("Relatórios").description("Relatórios agregados da carteira (dashboard)")
            ));
    }
}
