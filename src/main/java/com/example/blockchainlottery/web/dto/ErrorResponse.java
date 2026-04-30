package com.example.blockchainlottery.web.dto;

import java.time.Instant;

public record ErrorResponse(String error, String message, Instant timestamp) {
}
