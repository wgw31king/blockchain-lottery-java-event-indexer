package com.example.blockchainlottery.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lotteryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("blockchain-lottery-java API")
                        .version("v1")
                        .description("Production-ready EVM event ingestion and query backend"));
    }
}
