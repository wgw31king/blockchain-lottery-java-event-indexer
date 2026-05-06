package com.example.blockchainlottery.infrastructure.alerting;

import com.example.blockchainlottery.infrastructure.config.AlertingProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeAlertNotifier implements AlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(CompositeAlertNotifier.class);

    private final AlertingProperties properties;
    private final EmailAlertNotifier emailAlertNotifier; // optional when mail auto-config absent
    private final SlackAlertNotifier slackAlertNotifier;
    private final AtomicReference<Instant> lastSent = new AtomicReference<>(Instant.EPOCH);

    public CompositeAlertNotifier(
            AlertingProperties properties,
            EmailAlertNotifier emailAlertNotifier,
            SlackAlertNotifier slackAlertNotifier
    ) {
        this.properties = properties;
        this.emailAlertNotifier = emailAlertNotifier;
        this.slackAlertNotifier = slackAlertNotifier;
    }

    @Override
    public void notify(String subject, String body) {
        Instant now = Instant.now();
        Instant previous = lastSent.get();
        long cooldownMs = properties.getCooldownMs();
        if (Duration.between(previous, now).toMillis() < cooldownMs) {
            log.debug("Skipping alert due to cooldown: {}", subject);
            return;
        }
        if (!lastSent.compareAndSet(previous, now)) {
            return;
        }
        if (properties.getEmail().isEnabled() && emailAlertNotifier != null) {
            emailAlertNotifier.send(subject, body);
        }
        if (properties.getSlack().isEnabled()) {
            slackAlertNotifier.send(subject, body);
        }
    }
}
