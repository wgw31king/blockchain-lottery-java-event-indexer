package com.example.blockchainlottery;

import com.example.blockchainlottery.config.LotteryChainProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(LotteryChainProperties.class)
public class BlockchainLotteryApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainLotteryApplication.class, args);
    }
}
