package com.example.blockchainlottery.infrastructure.decoder;

import java.util.Map;

public record DecodedEvent(
        String name,
        String signature,
        Map<String, Object> decodedParams,
        String fromAddress,
        String toAddress
) {
}
