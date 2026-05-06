package com.example.blockchainlottery.infrastructure.decoder;

import com.example.blockchainlottery.infrastructure.config.LotteryChainProperties.EventDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class AbiJsonEventLoader {

    private static final Logger log = LoggerFactory.getLogger(AbiJsonEventLoader.class);

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public AbiJsonEventLoader(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    public List<EventDefinition> load(String abiJsonPath) {
        if (abiJsonPath == null || abiJsonPath.isBlank()) {
            return List.of();
        }
        Resource resource = resourceLoader.getResource(abiJsonPath);
        if (!resource.exists()) {
            log.warn("ABI resource not found: {}", abiJsonPath);
            return List.of();
        }
        try (InputStream in = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(in);
            if (!root.isArray()) {
                throw new IllegalArgumentException("ABI JSON must be an array");
            }
            List<EventDefinition> out = new ArrayList<>();
            for (JsonNode node : root) {
                if (!"event".equalsIgnoreCase(text(node, "type"))) {
                    continue;
                }
                EventDefinition def = toEventDefinition(node);
                if (def != null) {
                    out.add(def);
                }
            }
            log.info("Loaded {} event definitions from ABI {}", out.size(), abiJsonPath);
            return out;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read ABI from " + abiJsonPath, e);
        }
    }

    private static EventDefinition toEventDefinition(JsonNode node) {
        String name = text(node, "name");
        if (name == null || name.isBlank()) {
            return null;
        }
        JsonNode inputs = node.get("inputs");
        if (inputs == null || !inputs.isArray()) {
            return null;
        }
        StringBuilder sigTypes = new StringBuilder();
        List<String> indexed = new ArrayList<>();
        List<String> data = new ArrayList<>();
        boolean first = true;
        for (JsonNode input : inputs) {
            String inName = text(input, "name");
            String inType = text(input, "type");
            boolean isIndexed = input.has("indexed") && input.get("indexed").asBoolean();
            if (!first) {
                sigTypes.append(",");
            }
            first = false;
            sigTypes.append(inType);
            String param = (inName != null && !inName.isBlank() ? inName : "arg") + ":" + normalizeType(inType);
            if (isIndexed) {
                indexed.add(param);
            } else {
                data.add(param);
            }
        }
        EventDefinition def = new EventDefinition();
        def.setName(name);
        def.setSignature(name + "(" + sigTypes + ")");
        def.setIndexedParams(indexed);
        def.setDataParams(data);
        return def;
    }

    private static String normalizeType(String type) {
        if (type == null) {
            return "uint256";
        }
        return switch (type) {
            case "uint256[]" -> "uint256[]";
            default -> type;
        };
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
