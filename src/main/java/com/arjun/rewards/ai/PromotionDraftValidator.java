package com.arjun.rewards.ai;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Guardrails on LLM output. The model proposes, this class decides.
 * Generated values are never trusted without checking them against business limits.
 */
@Component
public class PromotionDraftValidator {

    public static final Set<String> CATEGORIES = Set.of("electronics", "grocery", "apparel", "home", "toys", "pharmacy");
    public static final BigDecimal MAX_MULTIPLIER = new BigDecimal("10");

    public List<String> validate(PromotionDraft d, LocalDate today) {
        List<String> errors = new ArrayList<>();
        if (d == null) {
            errors.add("model returned no draft");
            return errors;
        }
        if (d.name() == null || d.name().isBlank()) errors.add("name is required");
        if (d.category() == null || !CATEGORIES.contains(d.category().toLowerCase())) {
            errors.add("category must be one of " + CATEGORIES);
        }
        if (d.multiplier() == null || d.multiplier().compareTo(BigDecimal.ONE) <= 0
                || d.multiplier().compareTo(MAX_MULTIPLIER) > 0) {
            errors.add("multiplier must be greater than 1 and at most " + MAX_MULTIPLIER);
        }
        if (d.startDate() == null || d.endDate() == null) {
            errors.add("start and end dates are required");
        } else {
            if (d.endDate().isBefore(d.startDate())) errors.add("end date is before start date");
            if (d.endDate().isBefore(today)) errors.add("promotion has already ended");
        }
        return errors;
    }
}
