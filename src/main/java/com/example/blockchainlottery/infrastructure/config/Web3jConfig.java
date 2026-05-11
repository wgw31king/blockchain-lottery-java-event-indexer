package com.example.blockchainlottery.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

@Configuration
public class Web3jConfig {

    @Bean(name = "httpWeb3j", destroyMethod = "shutdown")
    public Web3j httpWeb3j(LotteryChainProperties properties) {
        return Web3j.build(new HttpService(properties.getRpcUrl()));
    }

    /**
     * WebSocket clients are only registered when chain listeners are enabled. Avoids startup connects when API-only mode.
     */
    @Configuration
    @ConditionalOnProperty(prefix = "lottery.chain", name = "listeners-enabled", havingValue = "true")
    static class LotteryChainWebSocketConfiguration {

        @Bean(name = "webSocketService", destroyMethod = "close")
        public WebSocketService webSocketService(LotteryChainProperties properties) {
            return new WebSocketService(properties.getWsUrl(), false);
        }

        @Bean(name = "wsWeb3j", destroyMethod = "shutdown")
        public Web3j wsWeb3j(WebSocketService webSocketService) {
            return Web3j.build(webSocketService);
        }
    }
}
