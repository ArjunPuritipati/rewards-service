package com.arjun.rewards.repository;

import com.arjun.rewards.domain.PromotionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PromotionRuleRepository extends JpaRepository<PromotionRule, Long> {

    @Query("select r from PromotionRule r where lower(r.category) = lower(:category) "
         + "and r.startDate <= :date and r.endDate >= :date")
    List<PromotionRule> findActive(@Param("category") String category, @Param("date") LocalDate date);
}
