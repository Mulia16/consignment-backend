package com.consignment.email.service;

import com.consignment.email.model.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailSenderService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void send(EmailRequest request) {
        try {
            Context ctx = new Context();
            if (request.variables() != null) {
                request.variables().forEach(ctx::setVariable);
            }
            String html = templateEngine.process(request.template(), ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(request.to());
            helper.setSubject(request.subject());
            helper.setText(html, true);
            mailSender.send(message);

            log.info("Email sent to {} with template {}", request.to(), request.template());
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", request.to(), e.getMessage());
            throw new RuntimeException("Email send failed", e);
        }
    }
}
