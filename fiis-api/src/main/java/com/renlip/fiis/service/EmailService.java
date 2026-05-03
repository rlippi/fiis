package com.renlip.fiis.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.renlip.fiis.config.MailProperties;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Envia emails transacionais renderizados por Thymeleaf.
 *
 * <p>Quando {@code fiis.mail.enabled=false} o serviço apenas loga o destinatário,
 * o assunto e o corpo HTML — útil em ambientes de desenvolvimento sem SMTP
 * configurado. Em HML/PROD o flag é {@code true} e os emails passam pelo
 * {@link JavaMailSender} auto-configurado pelo starter.</p>
 *
 * <p>O template é resolvido via {@link SpringTemplateEngine} (auto-configurado
 * pelo {@code spring-boot-starter-thymeleaf}). Passamos {@code nome}, {@code linkReset}
 * e {@code ttlMinutos} como variáveis do contexto; o template substitui os
 * atributos {@code th:*} por esses valores em runtime.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private static final String TEMPLATE_RESET_SENHA = "emails/reset-senha";

    private static final String ASSUNTO_RESET = "Redefinição de senha — FIIs";

    private final MailProperties mailProperties;

    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;

    /**
     * Renderiza e envia o email de redefinição de senha.
     *
     * @param destinatario endereço de email do usuário
     * @param nome         nome do usuário (exibido no corpo)
     * @param token        valor do {@code reset_token} a ser incluído no link
     * @param ttlMinutos   tempo de validade do token (exibido no texto)
     */
    public void enviarResetSenha(final String destinatario, final String nome,
                                 final String token, final long ttlMinutos) {
        String link = mailProperties.frontendUrl() + "/reset-senha?token=" + token;

        Context ctx = new Context(PT_BR);
        ctx.setVariable("nome", nome);
        ctx.setVariable("linkReset", link);
        ctx.setVariable("ttlMinutos", ttlMinutos);

        String html = templateEngine.process(TEMPLATE_RESET_SENHA, ctx);

        enviar(destinatario, ASSUNTO_RESET, html);
    }

    private void enviar(final String destinatario, final String assunto, final String corpoHtml) {
        if (!mailProperties.enabled()) {
            log.info("[email-desabilitado] para={} assunto={}\n{}", destinatario, assunto, corpoHtml);
            return;
        }

        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, false, StandardCharsets.UTF_8.name());
            helper.setFrom(mailProperties.fromAddress());
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);
            mailSender.send(mensagem);
        } catch (MessagingException e) {
            // Fluxo "esqueci senha" é idempotente do ponto de vista do usuário: em caso de falha
            // de SMTP, logamos mas NÃO propagamos o erro — assim não vazamos quais e-mails existem.
            log.error("Falha ao enviar email transacional para {}", destinatario, e);
        }
    }
}
