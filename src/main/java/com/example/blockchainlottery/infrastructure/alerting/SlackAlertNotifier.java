package com.example.blockchainlottery.infrastructure.alerting;

import com.example.blockchainlottery.infrastructure.config.AlertingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SlackAlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(SlackAlertNotifier.class);

    private final AlertingProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public SlackAlertNotifier(AlertingProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public void send(String subject, String body) {
        String url = properties.getSlack().getWebhookUrl();
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(Map.of("text", "*" + subject + "*\n" + body));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                log.warn("Slack webhook returned {}", resp.statusCode());
            }
        } catch (Exception e) {
            log.warn("Failed to send Slack alert: {}", e.getMessage());
        }
    }
}
