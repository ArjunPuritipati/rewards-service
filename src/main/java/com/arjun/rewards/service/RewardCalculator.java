package com.arjun.rewards.service;

import com.arjun.rewards.domain.PromotionRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Pure points logic, kept free of the database so it is easy to unit test.
 * Base rate is 1 point per dollar. If several promotions match, the highest multiplier wins.
 */
@Component
public class RewardCalculator {

    public record Result(long points, Optional<PromotionRule> appliedRule) { }

    public Result calculate(BigDecimal amount, List<PromotionRule> activeRules) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        Optional<PromotionRule> best = activeRules.stream()
                .max(Comparator.comparing(PromotionRule::getMultiplier));
        BigDecimal multiplier = best.map(PromotionRule::getMultiplier).orElse(BigDecimal.ONE);
        long points = amount.multiply(multiplier).setScale(0, RoundingMode.FLOOR).longValueExact();
        return new Result(points, best);
    }
}
