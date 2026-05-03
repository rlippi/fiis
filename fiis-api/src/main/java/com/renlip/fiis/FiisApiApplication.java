package com.renlip.fiis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Classe principal que inicia a aplicação Spring Boot.
 *
 * @SpringBootApplication faz 3 coisas de uma vez:
 * 1. @Configuration  - permite registrar beans no contexto
 * 2. @EnableAutoConfiguration - configura automaticamente com base nas dependências
 * 3. @ComponentScan - escaneia pacotes a partir daqui buscando @Controller, @Service, etc.
 *
 * @ConfigurationPropertiesScan habilita a descoberta automática de classes anotadas
 * com @ConfigurationProperties (ex: BrapiProperties), dispensando o @EnableConfigurationProperties
 * caso a caso.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class FiisApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiisApiApplication.class, args);
    }
}
