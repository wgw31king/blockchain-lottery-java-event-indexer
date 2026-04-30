package com.example.blockchainlottery.config;

import java.net.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

@Configuration
public class Web3jConfig {

    private static final Logger log = LoggerFactory.getLogger(Web3jConfig.class);

    @Bean(name = "httpWeb3j", destroyMethod = "shutdown")
    public Web3j httpWeb3j(LotteryChainProperties properties) {
        return Web3j.build(new HttpService(properties.getRpcUrl()));
    }

    @Bean(name = "webSocketService", destroyMethod = "close")
    public WebSocketService webSocketService(LotteryChainProperties properties) {
        WebSocketService webSocketService = new WebSocketService(properties.getWsUrl(), false);
        try {
            webSocketService.connect();
        } catch (ConnectException e) {
            log.warn("Initial WS connect failed, service will reconnect lazily: {}", e.getMessage());
        }
        return webSocketService;
    }

    @Bean(name = "wsWeb3j", destroyMethod = "shutdown")
    public Web3j wsWeb3j(WebSocketService webSocketService) {
        return Web3j.build(webSocketService);
    }
}
