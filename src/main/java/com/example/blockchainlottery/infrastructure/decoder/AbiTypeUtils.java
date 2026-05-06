package com.example.blockchainlottery.infrastructure.decoder;

import java.util.List;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.DynamicArray;

public final class AbiTypeUtils {

    private AbiTypeUtils() {
    }

    @SuppressWarnings("rawtypes")
    public static TypeReference<Type> toTypeReference(String solidityType) {
        return switch (solidityType) {
            case "address" -> cast(TypeReference.create(Address.class));
            case "uint256" -> cast(TypeReference.create(Uint256.class));
            case "bool" -> cast(TypeReference.create(Bool.class));
            case "bytes32" -> cast(TypeReference.create(Bytes32.class));
            case "bytes" -> cast(TypeReference.create(DynamicBytes.class));
            case "string" -> cast(TypeReference.create(Utf8String.class));
            case "uint256[]" -> cast(new TypeReference<DynamicArray<Uint256>>() { });
            default -> throw new IllegalArgumentException("Unsupported solidity type: " + solidityType);
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static TypeReference<Type> cast(TypeReference<?> typeReference) {
        return (TypeReference<Type>) (TypeReference<?>) typeReference;
    }

    public static String parseName(String paramDef) {
        List<String> parts = List.of(paramDef.split(":"));
        if (parts.size() != 2) {
            throw new IllegalArgumentException("Param definition should be name:type, got: " + paramDef);
        }
        return parts.get(0);
    }

    public static String parseType(String paramDef) {
        List<String> parts = List.of(paramDef.split(":"));
        if (parts.size() != 2) {
            throw new IllegalArgumentException("Param definition should be name:type, got: " + paramDef);
        }
        return parts.get(1);
    }

    public static Object normalizeValue(Type<?> type) {
        Object value = type.getValue();
        if (value instanceof byte[] bytes) {
            return "0x" + org.web3j.utils.Numeric.toHexStringNoPrefix(bytes);
        }
        if (value instanceof org.web3j.abi.datatypes.DynamicArray<?> arr) {
            List<?> vals = arr.getValue();
            return vals.stream().map(v -> v instanceof Type<?> t ? normalizeValue(t) : v).toList();
        }
        return value;
    }
}
