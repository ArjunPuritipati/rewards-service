package com.arjun.rewards.kafka;

import com.arjun.rewards.ai.PromotionParser;
import com.arjun.rewards.service.RewardService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/** Runs a real in-memory Kafka broker and drives the listener end to end. */
@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 3, topics = {"reward-transactions", "reward-transactions-dlt"})
class KafkaIngestionIntegrationTest {

    @Autowired KafkaTemplate<Object, Object> kafka;
    @Autowired RewardService rewardService;
    @Autowired EmbeddedKafkaBroker broker;
    @MockitoBean PromotionParser parser;

    private final LocalDate day = LocalDate.of(2026, 11, 14);

    @Test
    void eventIsScoredAndRedeliveryIsIgnored() {
        var event = new TransactionEvent("evt-1", "kafka-cust", new BigDecimal("75.00"), "grocery", day);
        kafka.send("reward-transactions", event.customerId(), event);
        kafka.send("reward-transactions", event.customerId(), event); // simulated redelivery

        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(rewardService.balance("kafka-cust")).isEqualTo(75));
        // give the duplicate time to be consumed, then confirm it did not double the balance
        await().pollDelay(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(rewardService.balance("kafka-cust")).isEqualTo(75));
    }

    @Test
    void invalidEventGoesToDeadLetterTopic() {
        var bad = new TransactionEvent("evt-bad", "dlt-cust", new BigDecimal("-10.00"), "grocery", day);
        kafka.send("reward-transactions", bad.customerId(), bad);

        Map<String, Object> props = KafkaTestUtils.consumerProps("dlt-checker", "true", broker);
        props.put("auto.offset.reset", "earliest");
        try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), new StringDeserializer()).createConsumer()) {
            broker.consumeFromAnEmbeddedTopic(consumer, "reward-transactions-dlt");
            ConsumerRecord<String, String> dead =
                    KafkaTestUtils.getSingleRecord(consumer, "reward-transactions-dlt", Duration.ofSeconds(15));
            assertThat(dead.key()).isEqualTo("dlt-cust");
            assertThat(dead.value()).contains("evt-bad");
        }
        assertThat(rewardService.balance("dlt-cust")).isZero();
    }
}
