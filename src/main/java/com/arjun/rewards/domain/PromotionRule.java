package com.arjun.rewards.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** A promotion that multiplies base points for a product category within a date window. */
@Entity
@Table(name = "promotion_rules")
public class PromotionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal multiplier;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    protected PromotionRule() { }

    public PromotionRule(String name, String category, BigDecimal multiplier, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.category = category.toLowerCase();
        this.multiplier = multiplier;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public BigDecimal getMultiplier() { return multiplier; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}
