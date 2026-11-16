package com.arjun.rewards.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Spring AI implementation. Structured output maps the response straight onto PromotionDraft. */
@Component
public class LlmPromotionParser implements PromotionParser {

    private static final String SYSTEM = """
            You convert retail promotion descriptions into structured data.
            Allowed categories: %s.
            The multiplier is the points multiplier (for example "double points" is 2).
            Resolve relative dates such as "this weekend" using today's date: %s.
            Use ISO dates. Never use a category outside the allowed list.
            """;

    private final ChatClient chatClient;

    public LlmPromotionParser(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public PromotionDraft parse(String description, LocalDate today) {
        return chatClient.prompt()
                .system(SYSTEM.formatted(PromotionDraftValidator.CATEGORIES, today))
                .user(description)
                .call()
                .entity(PromotionDraft.class);
    }
}
