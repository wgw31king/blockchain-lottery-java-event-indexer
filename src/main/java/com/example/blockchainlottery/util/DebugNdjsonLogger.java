package com.example.blockchainlottery.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class DebugNdjsonLogger {

    private static final Path LOG_PATH = Path.of("/Users/wahhh/blockchain-lottery-java/.cursor/debug-f40485.log");

    private DebugNdjsonLogger() {
    }

    public static void log(String runId, String hypothesisId, String location, String message, String dataJson) {
        String line = String.format(
                "{\"sessionId\":\"f40485\",\"runId\":\"%s\",\"hypothesisId\":\"%s\",\"location\":\"%s\",\"message\":\"%s\",\"data\":%s,\"timestamp\":%d}%n",
                escape(runId),
                escape(hypothesisId),
                escape(location),
                escape(message),
                dataJson == null ? "{}" : dataJson,
                System.currentTimeMillis()
        );
        try {
            Files.writeString(LOG_PATH, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ignored) {
            // no-op for debug logging failures
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
