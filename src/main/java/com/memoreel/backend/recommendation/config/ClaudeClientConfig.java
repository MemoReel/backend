package com.memoreel.backend.recommendation.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memoreel.backend.recommendation.adapter.claude.ClaudePromptBuilder;
import com.memoreel.backend.recommendation.adapter.claude.ClaudeProperties;
import com.memoreel.backend.recommendation.adapter.claude.ClaudeResponseParser;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** {@code memoreel.llm.provider=claude}일 때만 활성화되는 Anthropic 클라이언트 빈 설정. */
@Configuration
@ConditionalOnProperty(name = "memoreel.llm.provider", havingValue = "claude")
@EnableConfigurationProperties(ClaudeProperties.class)
public class ClaudeClientConfig {

  @Bean
  public AnthropicClient anthropicClient(ClaudeProperties properties) {
    if (properties.apiKey() == null || properties.apiKey().isBlank()) {
      throw new IllegalStateException("memoreel.llm.provider=claude인데 ANTHROPIC_API_KEY가 비어 있습니다.");
    }
    return AnthropicOkHttpClient.builder()
        .apiKey(properties.apiKey())
        .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
        .maxRetries(properties.maxRetries())
        .build();
  }

  @Bean
  public ClaudePromptBuilder claudePromptBuilder(ObjectMapper objectMapper) {
    return new ClaudePromptBuilder(objectMapper);
  }

  @Bean
  public ClaudeResponseParser claudeResponseParser() {
    return new ClaudeResponseParser();
  }
}
