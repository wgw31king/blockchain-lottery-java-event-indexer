package com.example.blockchainlottery.infrastructure.alerting;

public interface AlertNotifier {

    void notify(String subject, String body);
}
