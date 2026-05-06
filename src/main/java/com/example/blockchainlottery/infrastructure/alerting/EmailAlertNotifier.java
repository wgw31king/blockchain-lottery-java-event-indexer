package com.example.blockchainlottery.infrastructure.alerting;

import com.example.blockchainlottery.infrastructure.config.AlertingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(JavaMailSender.class)
public class EmailAlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(EmailAlertNotifier.class);

    private final JavaMailSender mailSender;
    private final AlertingProperties properties;

    public EmailAlertNotifier(JavaMailSender mailSender, AlertingProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void send(String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(properties.getEmail().getFrom());
            msg.setTo(properties.getEmail().getTo());
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send alert email: {}", e.getMessage());
        }
    }
}
