package com.memoreel.backend.recommendation.adapter.claude;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.ToolUseBlock;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import com.memoreel.backend.recommendation.port.LlmAnalysis;
import com.memoreel.backend.recommendation.port.SongCandidate;
import java.util.List;

/**
 * Claude Message 응답에서 지정된 tool_use 블록의 input을 추출해 도메인 객체로 매핑한다 (스펙 §5).
 *
 * <p>tool_use 누락 / JSON 파싱 실패 / 빈 candidates는 {@link ErrorCode#AI_ANALYSIS_FAILED}로 변환한다.
 */
public class ClaudeResponseParser {

  public LlmAnalysis parseAnalyzePhoto(Message message) {
    AnalyzePhotoInput input =
        extractToolInput(
            message, ClaudeToolSchemas.ANALYZE_PHOTO_TOOL_NAME, AnalyzePhotoInput.class);
    List<SongCandidate> candidates = toCandidates(input.candidates());
    if (candidates.isEmpty()) {
      throw new BusinessException(ErrorCode.AI_ANALYSIS_FAILED, "LLM 응답에 곡 후보 없음", null);
    }
    List<String> keywordNames =
        input.keywordNames() == null ? List.of() : List.copyOf(input.keywordNames());
    return new LlmAnalysis(input.description(), keywordNames, candidates);
  }

  public List<SongCandidate> parseRecommendSongs(Message message) {
    RecommendSongsInput input =
        extractToolInput(
            message, ClaudeToolSchemas.RECOMMEND_SONGS_TOOL_NAME, RecommendSongsInput.class);
    List<SongCandidate> candidates = toCandidates(input.candidates());
    if (candidates.isEmpty()) {
      throw new BusinessException(ErrorCode.AI_ANALYSIS_FAILED, "LLM 응답에 곡 후보 없음", null);
    }
    return candidates;
  }

  private <T> T extractToolInput(Message message, String toolName, Class<T> type) {
    ToolUseBlock toolUse =
        message.content().stream()
            .map(ContentBlock::toolUse)
            .filter(java.util.Optional::isPresent)
            .map(java.util.Optional::get)
            .filter(b -> toolName.equals(b.name()))
            .findFirst()
            .orElseThrow(
                () -> new BusinessException(ErrorCode.AI_ANALYSIS_FAILED, "LLM 응답 형식 오류", null));

    JsonValue input = toolUse._input();
    try {
      return input.convert(type);
    } catch (RuntimeException e) {
      throw new BusinessException(ErrorCode.AI_ANALYSIS_FAILED, "LLM 응답 형식 오류", null);
    }
  }

  private List<SongCandidate> toCandidates(List<RawCandidate> raw) {
    if (raw == null) {
      return List.of();
    }
    return raw.stream()
        .filter(c -> isNotBlank(c.title()) && isNotBlank(c.artist()))
        .map(c -> new SongCandidate(c.title(), c.artist()))
        .toList();
  }

  private static boolean isNotBlank(String value) {
    return value != null && !value.isBlank();
  }

  record RawCandidate(String title, String artist) {}

  record AnalyzePhotoInput(
      String description,
      @JsonProperty("keyword_names") List<String> keywordNames,
      List<RawCandidate> candidates) {}

  record RecommendSongsInput(List<RawCandidate> candidates) {}
}
