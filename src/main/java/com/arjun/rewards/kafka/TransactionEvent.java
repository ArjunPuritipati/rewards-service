package com.arjun.rewards.kafka;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Message published by point-of-sale or checkout systems when a purchase completes. */
public record TransactionEvent(String eventId, String customerId, BigDecimal amount, String category, LocalDate purchaseDate) { }
