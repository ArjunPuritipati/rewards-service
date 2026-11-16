# Rewards Service

A Java 21 / Spring Boot 3 service that scores retail purchases for reward points, with an AI step that turns plain-language promotions into validated rules.

## What it does

1. `POST /api/transactions` records a purchase and awards points (1 per dollar, times the best active promotion).
2. `GET /api/customers/{id}/points` returns a customer's balance.
3. `POST /api/promotions/parse` sends a description like "double points on electronics this weekend" to an LLM through Spring AI. The model returns a structured draft, which is checked against business rules (allowed categories, multiplier cap, valid dates). Nothing is saved at this step.
4. `POST /api/promotions` saves an approved draft after validating it again.

5. Kafka topic `reward-transactions` ingests purchase events from checkout systems. Processing is idempotent by `eventId`, transient failures are retried, and bad events go to `reward-transactions-dlt`.

The design keeps the LLM out of the money path: it only proposes rules, and deterministic Java code decides what is valid and how points are calculated.

## Run locally

```bash
export OPENAI_API_KEY=sk-...        # only needed for /parse
mvn spring-boot:run                  # uses in-memory H2
```

With PostgreSQL:

```bash
docker compose up --build
```

## Try it

```bash
curl -X POST localhost:8080/api/promotions/parse -H 'Content-Type: application/json' \
  -d '{"description":"triple points on toys from Nov 20 to Nov 30"}'

curl -X POST localhost:8080/api/transactions -H 'Content-Type: application/json' \
  -d '{"customerId":"c1","amount":42.50,"category":"toys"}'

curl localhost:8080/api/customers/c1/points
```

## Send a Kafka event

With `docker compose up --build` running:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server kafka:9092 --topic reward-transactions
> {"eventId":"evt-100","customerId":"c2","amount":60.00,"category":"grocery","purchaseDate":"2026-11-14"}

curl localhost:8080/api/customers/c2/points
```

Running the app outside Docker? Start only Kafka (`docker compose up kafka`) and the app connects on `localhost:29092`.

## Observability

Actuator and Micrometer expose `rewards.points.awarded`, `rewards.events.duplicate`, and `rewards.transaction.processing` at `/actuator/prometheus`.

## Tests

`mvn verify` runs unit tests (points math, LLM output guardrails), a web-layer test with MockMvc, and an end-to-end JPA test, and an embedded-Kafka test covering ingestion, duplicate redelivery, and dead-lettering. The LLM is mocked in tests so they are deterministic and need no API key. CI runs the same command on every push through GitHub Actions.

## Ideas to extend

- Redis cache for active promotions
- Cassandra for the transaction ledger at higher volume
- Load test with k6 or Gatling and record p95 latency
