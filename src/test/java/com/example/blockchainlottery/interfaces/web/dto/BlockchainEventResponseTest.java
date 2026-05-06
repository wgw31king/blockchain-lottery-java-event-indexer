package com.example.blockchainlottery.interfaces.web.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.blockchainlottery.domain.BlockchainEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BlockchainEventResponseTest {

    @Test
    void masksAddressesWhenEnabled() {
        BlockchainEvent e = new BlockchainEvent();
        e.setChainId("1");
        e.setBlockNumber(1L);
        e.setBlockHash("0xbb");
        e.setTxHash("0xaa");
        e.setLogIndex(0L);
        e.setContractAddress("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd");
        e.setTopic0("0xt");
        e.setEventSignature("sig");
        e.setEventName("E");
        e.setFromAddress("0x1234567890123456789012345678901234567890");
        e.setToAddress(null);
        e.setRemoved(false);
        e.setRawData("0x");
        e.setDecodedParamsJson("{}");
        e.setProcessedAt(Instant.now());

        BlockchainEventResponse r = BlockchainEventResponse.from(e, true);
        assertTrue(r.fromAddress().contains("…"));
        assertEquals("0xabcd…abcd", r.contractAddress());
    }

    @Test
    void noMaskWhenDisabled() {
        BlockchainEvent e = new BlockchainEvent();
        e.setChainId("1");
        e.setBlockNumber(1L);
        e.setBlockHash("0xbb");
        e.setTxHash("0xaa");
        e.setLogIndex(0L);
        e.setContractAddress("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd");
        e.setTopic0("0xt");
        e.setEventSignature("sig");
        e.setEventName("E");
        e.setFromAddress("0x1234");
        e.setRawData("0x");
        e.setDecodedParamsJson("{}");
        e.setProcessedAt(Instant.now());

        BlockchainEventResponse r = BlockchainEventResponse.from(e, false);
        assertEquals("0x1234", r.fromAddress());
    }
}
