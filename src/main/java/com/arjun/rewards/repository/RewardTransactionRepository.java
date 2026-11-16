package com.arjun.rewards.repository;

import com.arjun.rewards.domain.RewardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {

    @Query("select coalesce(sum(t.pointsAwarded), 0) from RewardTransaction t where t.customerId = :customerId")
    long totalPoints(@Param("customerId") String customerId);

    boolean existsByEventId(String eventId);
}
