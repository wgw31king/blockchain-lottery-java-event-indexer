package com.example.blockchainlottery.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.api")
public class ApiProperties {

    private boolean maskAddresses;

    public boolean isMaskAddresses() {
        return maskAddresses;
    }

    public void setMaskAddresses(boolean maskAddresses) {
        this.maskAddresses = maskAddresses;
    }
}
