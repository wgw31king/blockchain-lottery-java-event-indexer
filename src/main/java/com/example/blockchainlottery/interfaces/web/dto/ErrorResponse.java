package com.example.blockchainlottery.interfaces.web.dto;

import java.time.Instant;

public record ErrorResponse(String error, String message, Instant timestamp) {
}
