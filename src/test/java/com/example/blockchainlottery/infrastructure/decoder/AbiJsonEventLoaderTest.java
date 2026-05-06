package com.example.blockchainlottery.infrastructure.decoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.example.blockchainlottery.infrastructure.config.LotteryChainProperties.EventDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class AbiJsonEventLoaderTest {

    @Test
    void loadsEventsFromClasspath() {
        AbiJsonEventLoader loader = new AbiJsonEventLoader(new ObjectMapper(), new DefaultResourceLoader());
        List<EventDefinition> defs = loader.load("classpath:abi/test-events.json");
        assertFalse(defs.isEmpty());
        assertEquals("CustomEvt", defs.get(0).getName());
        assertEquals("CustomEvt(address,uint256)", defs.get(0).getSignature());
    }
}
