package com.renlip.fiis.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Configuração do {@link MessageSource} usado para resolver as mensagens do arquivo
 * {@code messages_pt_BR.properties}.
 *
 * <p>Registra o bean explicitamente porque o auto-config do Spring Boot só cria um
 * {@link MessageSource} se encontrar o arquivo base {@code messages.properties} — o
 * que não é o caso aqui, já que utilizamos apenas a variante localizada.</p>
 */
@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setUseCodeAsDefaultMessage(false);
        return source;
    }
}
