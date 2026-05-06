package com.example.blockchainlottery.infrastructure.plugin;

import com.example.blockchainlottery.infrastructure.config.LotteryChainProperties.EventDefinition;
import java.util.List;

/**
 * Optional extension point for additional contract-specific event definitions or filters.
 */
public interface ContractProfilePlugin {

    /**
     * Extra {@link EventDefinition} entries merged after YAML and ABI loading.
     */
    default List<EventDefinition> additionalEventDefinitions() {
        return List.of();
    }
}
