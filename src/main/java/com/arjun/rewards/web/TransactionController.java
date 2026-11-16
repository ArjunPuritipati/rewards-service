package com.arjun.rewards.web;

import com.arjun.rewards.domain.RewardTransaction;
import com.arjun.rewards.service.RewardService;
import com.arjun.rewards.web.Dtos.BalanceResponse;
import com.arjun.rewards.web.Dtos.TransactionRequest;
import com.arjun.rewards.web.Dtos.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class TransactionController {

    private final RewardService rewardService;

    public TransactionController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse record(@Valid @RequestBody TransactionRequest req) {
        LocalDate date = req.purchaseDate() != null ? req.purchaseDate() : LocalDate.now();
        RewardTransaction tx = rewardService.record(req.customerId(), req.amount(), req.category(), date);
        return new TransactionResponse(tx.getId(), tx.getCustomerId(), tx.getPointsAwarded(), tx.getAppliedRuleId());
    }

    @GetMapping("/customers/{customerId}/points")
    public BalanceResponse balance(@PathVariable String customerId) {
        return new BalanceResponse(customerId, rewardService.balance(customerId));
    }
}
