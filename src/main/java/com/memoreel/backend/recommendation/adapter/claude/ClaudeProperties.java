package com.memoreel.backend.recommendation.adapter.claude;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Claude LLM 어댑터 설정. {@code memoreel.llm.claude.*} 키와 매핑된다. */
@ConfigurationProperties("memoreel.llm.claude")
public record ClaudeProperties(
    String apiKey, String model, int maxTokens, int timeoutSeconds, int maxRetries) {}
