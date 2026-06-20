package com.memoreel.backend.recommendation.adapter.claude;

import static org.assertj.core.api.Assertions.assertThat;

import com.memoreel.backend.recommendation.port.SongCandidate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class ClaudePromptBuilderTest {

  private ObjectMapper objectMapper;
  private ClaudePromptBuilder builder;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    builder = new ClaudePromptBuilder(objectMapper);
  }

  @Test
  void analyzeSystemPrompt_키워드_목록을_시스템_프롬프트에_주입한다() {
    String prompt = builder.analyzeSystemPrompt(List.of("노을", "여행"));

    assertThat(prompt).contains("노을, 여행").contains("analyze_photo");
  }

  @Test
  void recommendUserMessage_excludeTracks를_JSON으로_직렬화한다() throws Exception {
    var message =
        builder.recommendUserMessage(
            "노을 진 바다", List.of("노을"), List.of(new SongCandidate("Beach", "The Neighbourhood")));

    String json = message.content().string().orElseThrow();
    JsonNode node = objectMapper.readTree(json);
    assertThat(node.get("description").asText()).isEqualTo("노을 진 바다");
    assertThat(node.get("keyword_names").get(0).asText()).isEqualTo("노을");
    assertThat(node.get("excluded").get(0).get("title").asText()).isEqualTo("Beach");
    assertThat(node.get("excluded").get(0).get("artist").asText()).isEqualTo("The Neighbourhood");
  }
}
