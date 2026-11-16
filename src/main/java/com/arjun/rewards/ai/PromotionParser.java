package com.arjun.rewards.ai;

import java.time.LocalDate;

/** Turns a plain-language promotion description into a structured draft. */
public interface PromotionParser {
    PromotionDraft parse(String description, LocalDate today);
}
