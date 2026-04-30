package com.example.blockchainlottery.service.decoder;

import com.example.blockchainlottery.config.LotteryChainProperties;
import com.example.blockchainlottery.util.DebugNdjsonLogger;
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

    private final Map<String, EventDescriptor> byTopic0 = new HashMap<>();

    public AbiEventDecoderRegistry(LotteryChainProperties properties) {
        for (LotteryChainProperties.EventDefinition eventDefinition : properties.getEventDefinitions()) {
            EventDescriptor descriptor = EventDescriptor.from(eventDefinition);
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
        // #region agent log
        DebugNdjsonLogger.log(
                "compile-run",
                "H4",
                "AbiEventDecoderRegistry.decode",
                "decode descriptor matched",
                "{\"eventName\":\"" + descriptor.name() + "\",\"topic0\":\"" + topic0 + "\"}"
        );
        // #endregion

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
        static EventDescriptor from(LotteryChainProperties.EventDefinition eventDefinition) {
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
