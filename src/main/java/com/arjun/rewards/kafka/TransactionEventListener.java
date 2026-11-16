package com.arjun.rewards.kafka;

import com.arjun.rewards.service.RewardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TransactionEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventListener.class);

    private final RewardService rewardService;

    public TransactionEventListener(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @KafkaListener(topics = "${rewards.kafka.topic}", groupId = "rewards-service",
                   autoStartup = "${rewards.kafka.listener-auto-startup:true}")
    public void onTransaction(TransactionEvent event) {
        if (event.eventId() == null || event.eventId().isBlank()) {
            throw new IllegalArgumentException("eventId is required");
        }
        LocalDate date = event.purchaseDate() != null ? event.purchaseDate() : LocalDate.now();
        rewardService.recordEvent(event.eventId(), event.customerId(), event.amount(), event.category(), date)
                .ifPresentOrElse(
                        tx -> log.debug("event {} awarded {} points", event.eventId(), tx.getPointsAwarded()),
                        () -> log.info("event {} already processed, skipped", event.eventId()));
    }
}
