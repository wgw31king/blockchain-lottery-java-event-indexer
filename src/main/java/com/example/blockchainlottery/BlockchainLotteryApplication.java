package com.example.blockchainlottery;

import com.example.blockchainlottery.infrastructure.config.AlertingProperties;
import com.example.blockchainlottery.infrastructure.config.ApiProperties;
import com.example.blockchainlottery.infrastructure.config.AppSecurityProperties;
import com.example.blockchainlottery.infrastructure.config.LotteryChainProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({
        LotteryChainProperties.class,
        AppSecurityProperties.class,
        ApiProperties.class,
        AlertingProperties.class
})
public class BlockchainLotteryApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainLotteryApplication.class, args);
    }
}
