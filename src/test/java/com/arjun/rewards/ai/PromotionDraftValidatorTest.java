package com.arjun.rewards.ai;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionDraftValidatorTest {

    private final PromotionDraftValidator validator = new PromotionDraftValidator();
    private final LocalDate today = LocalDate.of(2026, 11, 14);

    @Test
    void acceptsWellFormedDraft() {
        var draft = new PromotionDraft("Weekend 2x", "electronics", new BigDecimal("2"), today, today.plusDays(1));
        assertThat(validator.validate(draft, today)).isEmpty();
    }

    @Test
    void rejectsHallucinatedCategoryAndExcessiveMultiplier() {
        var draft = new PromotionDraft("Crazy deal", "spaceships", new BigDecimal("50"), today, today.plusDays(1));
        assertThat(validator.validate(draft, today)).hasSize(2);
    }

    @Test
    void rejectsInvertedOrExpiredDates() {
        var draft = new PromotionDraft("Old", "grocery", new BigDecimal("2"), today.minusDays(5), today.minusDays(10));
        assertThat(validator.validate(draft, today))
                .contains("end date is before start date", "promotion has already ended");
    }

    @Test
    void handlesNullModelOutput() {
        assertThat(validator.validate(null, today)).containsExactly("model returned no draft");
    }
}
