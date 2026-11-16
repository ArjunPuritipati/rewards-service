package com.arjun.rewards.web;

import com.arjun.rewards.ai.PromotionDraft;
import com.arjun.rewards.ai.PromotionDraftValidator;
import com.arjun.rewards.ai.PromotionParser;
import com.arjun.rewards.domain.PromotionRule;
import com.arjun.rewards.service.RewardService;
import com.arjun.rewards.web.Dtos.ParseRequest;
import com.arjun.rewards.web.Dtos.ParseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionParser parser;
    private final PromotionDraftValidator validator;
    private final RewardService rewardService;

    public PromotionController(PromotionParser parser, PromotionDraftValidator validator, RewardService rewardService) {
        this.parser = parser;
        this.validator = validator;
        this.rewardService = rewardService;
    }

    /** Step 1: the LLM turns plain language into a draft. Nothing is saved yet. */
    @PostMapping("/parse")
    public ParseResponse parse(@Valid @RequestBody ParseRequest req) {
        LocalDate today = LocalDate.now();
        PromotionDraft draft = parser.parse(req.description(), today);
        List<String> errors = validator.validate(draft, today);
        return new ParseResponse(draft, errors.isEmpty(), errors);
    }

    /** Step 2: a person approves the draft (edited if needed). It is re-validated, then saved. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionRule create(@RequestBody PromotionDraft draft) {
        List<String> errors = validator.validate(draft, LocalDate.now());
        if (!errors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        }
        return rewardService.savePromotion(new PromotionRule(
                draft.name(), draft.category(), draft.multiplier(), draft.startDate(), draft.endDate()));
    }

    @GetMapping
    public List<PromotionRule> list() {
        return rewardService.allPromotions();
    }
}
