package com.arjun.rewards.web;

import com.arjun.rewards.ai.PromotionDraft;
import com.arjun.rewards.ai.PromotionDraftValidator;
import com.arjun.rewards.ai.PromotionParser;
import com.arjun.rewards.service.RewardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromotionController.class)
@Import(PromotionDraftValidator.class)
class PromotionControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean PromotionParser parser;
    @MockitoBean RewardService rewardService;

    @Test
    void parseReturnsValidationErrorsInsteadOfSavingBadModelOutput() throws Exception {
        LocalDate today = LocalDate.now();
        when(parser.parse(anyString(), any())).thenReturn(
                new PromotionDraft("Bad", "spaceships", new BigDecimal("2"), today, today.plusDays(1)));

        mvc.perform(post("/api/promotions/parse").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"double points on spaceships\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]").exists());
    }

    @Test
    void createRejectsInvalidDraft() throws Exception {
        mvc.perform(post("/api/promotions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"category\":\"grocery\",\"multiplier\":99,"
                               + "\"startDate\":\"2030-01-01\",\"endDate\":\"2030-01-02\"}"))
                .andExpect(status().isBadRequest());
    }
}
