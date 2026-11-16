package com.arjun.rewards.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** A purchase and the points awarded for it. */
@Entity
@Table(name = "reward_transactions", indexes = @Index(name = "idx_customer", columnList = "customerId"))
public class RewardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    @Column(nullable = false)
    private long pointsAwarded;

    private Long appliedRuleId;

    /** Producer-assigned id for Kafka events. Unique so a redelivered event can never award points twice. */
    @Column(unique = true)
    private String eventId;

    protected RewardTransaction() { }

    public RewardTransaction(String customerId, BigDecimal amount, String category, LocalDate purchaseDate,
                             long pointsAwarded, Long appliedRuleId) {
        this.customerId = customerId;
        this.amount = amount;
        this.category = category;
        this.purchaseDate = purchaseDate;
        this.pointsAwarded = pointsAwarded;
        this.appliedRuleId = appliedRuleId;
    }

    public RewardTransaction(String customerId, BigDecimal amount, String category, LocalDate purchaseDate,
                             long pointsAwarded, Long appliedRuleId, String eventId) {
        this(customerId, amount, category, purchaseDate, pointsAwarded, appliedRuleId);
        this.eventId = eventId;
    }

    public Long getId() { return id; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public long getPointsAwarded() { return pointsAwarded; }
    public Long getAppliedRuleId() { return appliedRuleId; }
    public String getEventId() { return eventId; }
}
