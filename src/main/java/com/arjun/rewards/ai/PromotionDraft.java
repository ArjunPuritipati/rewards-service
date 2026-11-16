package com.arjun.rewards.ai;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Structured output the LLM must return. Always validated before it can become a PromotionRule. */
public record PromotionDraft(String name, String category, BigDecimal multiplier, LocalDate startDate, LocalDate endDate) { }
