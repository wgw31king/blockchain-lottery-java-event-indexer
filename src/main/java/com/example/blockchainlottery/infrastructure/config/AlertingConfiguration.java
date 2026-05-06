package com.example.blockchainlottery.infrastructure.config;

import com.example.blockchainlottery.infrastructure.alerting.AlertNotifier;
import com.example.blockchainlottery.infrastructure.alerting.CompositeAlertNotifier;
import com.example.blockchainlottery.infrastructure.alerting.EmailAlertNotifier;
import com.example.blockchainlottery.infrastructure.alerting.SlackAlertNotifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlertingConfiguration {

    @Bean
    public AlertNotifier alertNotifier(
            AlertingProperties properties,
            ObjectProvider<EmailAlertNotifier> emailAlertNotifier,
            SlackAlertNotifier slackAlertNotifier
    ) {
        if (!properties.isEnabled()) {
            return (subject, body) -> { };
        }
        return new CompositeAlertNotifier(properties, emailAlertNotifier.getIfAvailable(), slackAlertNotifier);
    }
}
