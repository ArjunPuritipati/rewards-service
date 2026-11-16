package com.arjun.rewards.service;

import com.arjun.rewards.domain.PromotionRule;
import com.arjun.rewards.domain.RewardTransaction;
import com.arjun.rewards.repository.PromotionRuleRepository;
import com.arjun.rewards.repository.RewardTransactionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RewardService {

    private final PromotionRuleRepository rules;
    private final RewardTransactionRepository transactions;
    private final RewardCalculator calculator;
    private final Counter pointsAwarded;
    private final Counter duplicateEvents;
    private final Timer processingTimer;

    public RewardService(PromotionRuleRepository rules, RewardTransactionRepository transactions,
                         RewardCalculator calculator, MeterRegistry registry) {
        this.rules = rules;
        this.transactions = transactions;
        this.calculator = calculator;
        this.pointsAwarded = Counter.builder("rewards.points.awarded")
                .description("Total reward points awarded").register(registry);
        this.duplicateEvents = Counter.builder("rewards.events.duplicate")
                .description("Kafka events skipped because they were already processed").register(registry);
        this.processingTimer = Timer.builder("rewards.transaction.processing")
                .description("Time to score and persist a transaction").register(registry);
    }

    /** REST path: every call is a new purchase. */
    @Transactional
    public RewardTransaction record(String customerId, BigDecimal amount, String category, LocalDate date) {
        return score(customerId, amount, category, date, null);
    }

    /**
     * Kafka path: Kafka delivers at least once, so the same event can arrive again after a rebalance or retry.
     * The eventId check (backed by a unique column) makes processing idempotent.
     */
    @Transactional
    public Optional<RewardTransaction> recordEvent(String eventId, String customerId, BigDecimal amount,
                                                   String category, LocalDate date) {
        if (transactions.existsByEventId(eventId)) {
            duplicateEvents.increment();
            return Optional.empty();
        }
        return Optional.of(score(customerId, amount, category, date, eventId));
    }

    private RewardTransaction score(String customerId, BigDecimal amount, String category, LocalDate date, String eventId) {
        return processingTimer.record(() -> {
            List<PromotionRule> active = rules.findActive(category, date);
            RewardCalculator.Result result = calculator.calculate(amount, active);
            Long ruleId = result.appliedRule().map(PromotionRule::getId).orElse(null);
            RewardTransaction saved = transactions.save(new RewardTransaction(
                    customerId, amount, category, date, result.points(), ruleId, eventId));
            pointsAwarded.increment(result.points());
            return saved;
        });
    }

    @Transactional(readOnly = true)
    public long balance(String customerId) {
        return transactions.totalPoints(customerId);
    }

    @Transactional
    public PromotionRule savePromotion(PromotionRule rule) {
        return rules.save(rule);
    }

    @Transactional(readOnly = true)
    public List<PromotionRule> allPromotions() {
        return rules.findAll();
    }
}
