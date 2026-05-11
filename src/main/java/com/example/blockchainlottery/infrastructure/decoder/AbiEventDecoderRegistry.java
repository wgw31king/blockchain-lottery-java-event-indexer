package com.example.blockchainlottery.infrastructure.decoder;

import com.example.blockchainlottery.infrastructure.config.LotteryChainProperties;
import com.example.blockchainlottery.infrastructure.config.LotteryChainProperties.EventDefinition;
import com.example.blockchainlottery.infrastructure.plugin.ContractProfilePlugin;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Hash;
import org.web3j.protocol.core.methods.response.Log;

@Component
public class AbiEventDecoderRegistry {

    private final LotteryChainProperties properties;
    private final AbiJsonEventLoader abiJsonEventLoader;
    private final List<ContractProfilePlugin> contractProfilePlugins;

    private final Map<String, EventDescriptor> byTopic0 = new HashMap<>();

    public AbiEventDecoderRegistry(
            LotteryChainProperties properties,
            AbiJsonEventLoader abiJsonEventLoader,
            List<ContractProfilePlugin> contractProfilePlugins
    ) {
        this.properties = properties;
        this.abiJsonEventLoader = abiJsonEventLoader;
        this.contractProfilePlugins = contractProfilePlugins;
    }

    @PostConstruct
    void registerAll() {
        List<EventDefinition> merged = new ArrayList<>();
        merged.addAll(properties.getEventDefinitions());
        merged.addAll(abiJsonEventLoader.load(properties.getAbiJsonPath()));
        merged.addAll(ErcStandardEvents.definitions(
                properties.isEnableErc721Transfer(),
                properties.isEnableErc1155TransferSingle(),
                properties.isEnableErc1155TransferBatch()
        ));
        for (ContractProfilePlugin plugin : contractProfilePlugins) {
            merged.addAll(plugin.additionalEventDefinitions());
        }
        for (EventDefinition def : merged) {
            EventDescriptor descriptor = EventDescriptor.from(def);
            byTopic0.put(descriptor.topic0(), descriptor);
        }
    }

    public List<String> supportedTopics() {
        return byTopic0.keySet().stream().toList();
    }

    @SuppressWarnings("rawtypes")
    public DecodedEvent decode(Log logObject) {
        String topic0 = logObject.getTopics() == null || logObject.getTopics().isEmpty()
                ? ""
                : logObject.getTopics().get(0).toLowerCase();
        EventDescriptor descriptor = byTopic0.get(topic0);
        if (descriptor == null) {
            return new DecodedEvent("UNKNOWN_EVENT", "UNKNOWN", Map.of("rawTopics", logObject.getTopics()), null, null);
        }

        Map<String, Object> decoded = new LinkedHashMap<>();

        for (int i = 0; i < descriptor.indexedParamNames().size(); i++) {
            int topicIndex = i + 1;
            if (logObject.getTopics().size() <= topicIndex) {
                continue;
            }
            String encoded = logObject.getTopics().get(topicIndex);
            Type<?> value = FunctionReturnDecoder.decodeIndexedValue(
                    encoded,
                    descriptor.indexedTypes().get(i)
            );
            decoded.put(descriptor.indexedParamNames().get(i), AbiTypeUtils.normalizeValue(value));
        }

        List<Type> dataValues = FunctionReturnDecoder.decode(logObject.getData(), descriptor.dataTypes());
        for (int i = 0; i < dataValues.size(); i++) {
            decoded.put(descriptor.dataParamNames().get(i), AbiTypeUtils.normalizeValue(dataValues.get(i)));
        }

        return new DecodedEvent(
                descriptor.name(),
                descriptor.signature(),
                decoded,
                asAddress(decoded.get("from"), decoded.get("player")),
                asAddress(decoded.get("to"), null)
        );
    }

    private String asAddress(Object first, Object second) {
        Object candidate = first != null ? first : second;
        if (candidate == null) {
            return null;
        }
        return String.valueOf(candidate);
    }

    @SuppressWarnings("rawtypes")
    private record EventDescriptor(
            String name,
            String signature,
            String topic0,
            List<TypeReference<Type>> indexedTypes,
            List<TypeReference<Type>> dataTypes,
            List<String> indexedParamNames,
            List<String> dataParamNames
    ) {
        static EventDescriptor from(EventDefinition eventDefinition) {
            List<TypeReference<Type>> indexedTypes = new ArrayList<>();
            List<TypeReference<Type>> dataTypes = new ArrayList<>();
            List<String> indexedParamNames = new ArrayList<>();
            List<String> dataParamNames = new ArrayList<>();

            for (String indexed : Optional.ofNullable(eventDefinition.getIndexedParams()).orElse(List.of())) {
                indexedParamNames.add(AbiTypeUtils.parseName(indexed));
                indexedTypes.add(AbiTypeUtils.toTypeReference(AbiTypeUtils.parseType(indexed)));
            }
            for (String data : Optional.ofNullable(eventDefinition.getDataParams()).orElse(List.of())) {
                dataParamNames.add(AbiTypeUtils.parseName(data));
                dataTypes.add(AbiTypeUtils.toTypeReference(AbiTypeUtils.parseType(data)));
            }

            String topic0 = Hash.sha3String(eventDefinition.getSignature()).toLowerCase();

            return new EventDescriptor(
                    eventDefinition.getName(),
                    eventDefinition.getSignature(),
                    topic0,
                    indexedTypes,
                    dataTypes,
                    indexedParamNames,
                    dataParamNames
            );
        }
    }
}
