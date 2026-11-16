package com.arjun.rewards.web;

import com.arjun.rewards.ai.PromotionDraft;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class Dtos {
    private Dtos() { }

    public record TransactionRequest(
            @NotBlank String customerId,
            @NotNull @Positive BigDecimal amount,
            @NotBlank String category,
            LocalDate purchaseDate) { }

    public record TransactionResponse(Long id, String customerId, long pointsAwarded, Long appliedRuleId) { }

    public record BalanceResponse(String customerId, long points) { }

    public record ParseRequest(@NotBlank @Size(max = 500) String description) { }

    public record ParseResponse(PromotionDraft draft, boolean valid, List<String> errors) { }
}
