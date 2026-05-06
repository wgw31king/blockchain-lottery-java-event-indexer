package com.example.blockchainlottery.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class LotteryChainPropertiesTest {

    @Test
    void dedupesContractAddresses() {
        LotteryChainProperties p = new LotteryChainProperties();
        p.setContractAddress("0xAAA");
        p.setAdditionalContractAddresses(List.of("0xBBB", "0xAAA", ""));

        assertEquals(2, p.allContractAddresses().size());
    }
}
