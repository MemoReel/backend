package com.memoreel.backend.recommendation.adapter.claude;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.ToolUseBlock;
import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import com.memoreel.backend.recommendation.port.LlmAnalysis;
import com.memoreel.backend.recommendation.port.SongCandidate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ClaudeResponseParserTest {

  private final ClaudeResponseParser parser = new ClaudeResponseParser();

  @Test
  void parseAnalyzePhoto_정상_응답을_LlmAnalysis로_변환한다() {
    Map<String, Object> input =
        Map.of(
            "description",
            "노을 진 바다",
            "keyword_names",
            List.of("노을", "여행"),
            "candidates",
            List.of(
                Map.of("title", "Beach", "artist", "The Neighbourhood"),
                Map.of("title", "Yellow", "artist", "Coldplay")));
    Message message = mockMessageWithToolUse(ClaudeToolSchemas.ANALYZE_PHOTO_TOOL_NAME, input);

    LlmAnalysis analysis = parser.parseAnalyzePhoto(message);

    assertThat(analysis.description()).isEqualTo("노을 진 바다");
    assertThat(analysis.keywordNames()).containsExactly("노을", "여행");
    assertThat(analysis.candidates())
        .containsExactly(
            new SongCandidate("Beach", "The Neighbourhood"),
            new SongCandidate("Yellow", "Coldplay"));
  }

  @Test
  void parseAnalyzePhoto_tool_use_없으면_AI_ANALYSIS_FAILED를_던진다() {
    Message message = mockMessageWithoutToolUse();

    assertThatThrownBy(() -> parser.parseAnalyzePhoto(message))
        .isInstanceOfSatisfying(
            BusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AI_ANALYSIS_FAILED));
  }

  @Test
  void parseAnalyzePhoto_candidates_비어있으면_AI_ANALYSIS_FAILED를_던진다() {
    Map<String, Object> input =
        Map.of("description", "노을 진 바다", "keyword_names", List.of("노을"), "candidates", List.of());
    Message message = mockMessageWithToolUse(ClaudeToolSchemas.ANALYZE_PHOTO_TOOL_NAME, input);

    assertThatThrownBy(() -> parser.parseAnalyzePhoto(message))
        .isInstanceOfSatisfying(
            BusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AI_ANALYSIS_FAILED));
  }

  @Test
  void parseRecommendSongs_정상_응답을_SongCandidate_리스트로_변환한다() {
    Map<String, Object> input =
        Map.of(
            "candidates",
            List.of(
                Map.of("title", "Sunflower", "artist", "Post Malone"),
                Map.of("title", "Stay", "artist", "The Kid LAROI")));
    Message message = mockMessageWithToolUse(ClaudeToolSchemas.RECOMMEND_SONGS_TOOL_NAME, input);

    List<SongCandidate> candidates = parser.parseRecommendSongs(message);

    assertThat(candidates)
        .containsExactly(
            new SongCandidate("Sunflower", "Post Malone"),
            new SongCandidate("Stay", "The Kid LAROI"));
  }

  @Test
  void parseRecommendSongs_title_또는_artist_빈문자열은_건너뛴다() {
    Map<String, Object> input =
        Map.of(
            "candidates",
            List.of(
                Map.of("title", "Sunflower", "artist", "Post Malone"),
                Map.of("title", "", "artist", "AAA"),
                Map.of("title", "BBB", "artist", "")));
    Message message = mockMessageWithToolUse(ClaudeToolSchemas.RECOMMEND_SONGS_TOOL_NAME, input);

    List<SongCandidate> candidates = parser.parseRecommendSongs(message);

    assertThat(candidates).containsExactly(new SongCandidate("Sunflower", "Post Malone"));
  }

  private Message mockMessageWithToolUse(String toolName, Map<String, Object> input) {
    ToolUseBlock toolUseBlock = mock(ToolUseBlock.class);
    when(toolUseBlock.name()).thenReturn(toolName);
    when(toolUseBlock._input()).thenReturn(JsonValue.from(input));

    ContentBlock contentBlock = mock(ContentBlock.class);
    when(contentBlock.toolUse()).thenReturn(java.util.Optional.of(toolUseBlock));

    Message message = mock(Message.class);
    when(message.content()).thenReturn(List.of(contentBlock));
    return message;
  }

  private Message mockMessageWithoutToolUse() {
    TextBlock textBlock = mock(TextBlock.class);
    ContentBlock contentBlock = mock(ContentBlock.class);
    when(contentBlock.toolUse()).thenReturn(java.util.Optional.empty());
    when(contentBlock.text()).thenReturn(java.util.Optional.of(textBlock));

    Message message = mock(Message.class);
    when(message.content()).thenReturn(List.of(contentBlock));
    return message;
  }
}
