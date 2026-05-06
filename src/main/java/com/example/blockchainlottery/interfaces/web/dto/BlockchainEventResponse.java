package com.example.blockchainlottery.interfaces.web.dto;

import com.example.blockchainlottery.domain.BlockchainEvent;
import java.time.Instant;

public record BlockchainEventResponse(
        Long id,
        String chainId,
        Long blockNumber,
        String blockHash,
        String txHash,
        Long logIndex,
        String contractAddress,
        String topic0,
        String eventSignature,
        String eventName,
        String fromAddress,
        String toAddress,
        boolean removed,
        String rawData,
        String decodedParamsJson,
        Instant processedAt
) {

    public static BlockchainEventResponse from(BlockchainEvent e, boolean maskAddresses) {
        return new BlockchainEventResponse(
                e.getId(),
                e.getChainId(),
                e.getBlockNumber(),
                e.getBlockHash(),
                e.getTxHash(),
                e.getLogIndex(),
                mask(e.getContractAddress(), maskAddresses),
                e.getTopic0(),
                e.getEventSignature(),
                e.getEventName(),
                mask(e.getFromAddress(), maskAddresses),
                mask(e.getToAddress(), maskAddresses),
                e.isRemoved(),
                e.getRawData(),
                e.getDecodedParamsJson(),
                e.getProcessedAt()
        );
    }

    private static String mask(String address, boolean mask) {
        if (!mask || address == null || address.length() < 10) {
            return address;
        }
        return address.substring(0, 6) + "…" + address.substring(address.length() - 4);
    }
}
