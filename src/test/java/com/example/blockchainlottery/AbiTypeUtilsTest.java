package com.example.blockchainlottery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.blockchainlottery.infrastructure.decoder.AbiTypeUtils;
import org.junit.jupiter.api.Test;

class AbiTypeUtilsTest {

    @Test
    void parseParamDefinition() {
        assertEquals("player", AbiTypeUtils.parseName("player:address"));
        assertEquals("address", AbiTypeUtils.parseType("player:address"));
    }
}
