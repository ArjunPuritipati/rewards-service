package com.arjun.rewards.service;

import com.arjun.rewards.domain.PromotionRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RewardCalculatorTest {

    private final RewardCalculator calculator = new RewardCalculator();
    private final LocalDate today = LocalDate.of(2026, 11, 14);

    private PromotionRule rule(String multiplier) {
        return new PromotionRule("promo", "electronics", new BigDecimal(multiplier), today, today.plusDays(2));
    }

    @Test
    void basePointsAreOnePerDollarRoundedDown() {
        var result = calculator.calculate(new BigDecimal("49.99"), List.of());
        assertThat(result.points()).isEqualTo(49);
        assertThat(result.appliedRule()).isEmpty();
    }

    @Test
    void highestMultiplierWinsWhenRulesOverlap() {
        var result = calculator.calculate(new BigDecimal("100.00"), List.of(rule("2"), rule("3.5")));
        assertThat(result.points()).isEqualTo(350);
        assertThat(result.appliedRule()).get().extracting(PromotionRule::getMultiplier)
                .isEqualTo(new BigDecimal("3.5"));
    }

    @Test
    void rejectsNonPositiveAmounts() {
        assertThatThrownBy(() -> calculator.calculate(BigDecimal.ZERO, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
