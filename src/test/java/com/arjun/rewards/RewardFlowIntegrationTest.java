package com.arjun.rewards;

import com.arjun.rewards.ai.PromotionParser;
import com.arjun.rewards.domain.PromotionRule;
import com.arjun.rewards.service.RewardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** End-to-end through JPA and H2. The LLM is mocked so the test is deterministic and needs no API key. */
@SpringBootTest(properties = {"rewards.kafka.listener-auto-startup=false", "spring.kafka.admin.auto-create=false"})
class RewardFlowIntegrationTest {

    @Autowired RewardService rewardService;
    @MockitoBean PromotionParser parser;

    @Test
    void activePromotionMultipliesPointsAndBalanceAccumulates() {
        LocalDate day = LocalDate.of(2026, 11, 14);
        rewardService.savePromotion(new PromotionRule("Weekend 2x", "electronics", new BigDecimal("2"), day, day.plusDays(1)));

        var promoTx = rewardService.record("cust-1", new BigDecimal("150.00"), "Electronics", day);
        var plainTx = rewardService.record("cust-1", new BigDecimal("20.00"), "grocery", day);

        assertThat(promoTx.getPointsAwarded()).isEqualTo(300);
        assertThat(promoTx.getAppliedRuleId()).isNotNull();
        assertThat(plainTx.getPointsAwarded()).isEqualTo(20);
        assertThat(rewardService.balance("cust-1")).isEqualTo(320);
    }
}
