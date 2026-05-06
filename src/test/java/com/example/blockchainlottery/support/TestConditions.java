package com.example.blockchainlottery.support;

public final class TestConditions {

    private TestConditions() {
    }

    /**
     * Used with {@link org.junit.jupiter.api.condition.EnabledIf} for optional Docker-dependent tests.
     */
    @SuppressWarnings("unused")
    public static boolean isDockerAvailable() {
        try {
            org.testcontainers.DockerClientFactory.instance().client();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
