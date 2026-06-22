package com.memoreel.backend.recommendation.adapter.claude;

import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.TextBlockParam;
import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import com.memoreel.backend.recommendation.port.SongCandidate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/** Claude 메시지 페이로드 조립 (스펙 §4). 키워드 목록은 외부에서 주입 후 시스템 프롬프트에 포함된다. */
public class ClaudePromptBuilder {

  private static final String ANALYZE_USER_TEXT = "이 사진을 분석해 분위기에 어울리는 곡을 추천해줘.";

  private final ObjectMapper objectMapper;

  public ClaudePromptBuilder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /** Stage 1 시스템 프롬프트. */
  public String analyzeSystemPrompt(List<String> keywordNames) {
    String joined = String.join(", ", keywordNames);
    return """
        너는 사진을 보고 분위기에 어울리는 음악을 추천하는 큐레이터다.
        반드시 analyze_photo tool을 호출해 응답해야 한다.
        규칙:
        - description: 사진의 분위기와 풍경을 한국어로 1~2문장 묘사한다.
        - keyword_names: 다음 목록에서만 1개 이상 3개 이하 선택한다. 목록 밖의 단어는 사용하지 않는다. [%s]
        - candidates: title/artist 쌍의 곡을 정확히 20개 제시한다. 분위기와 어울리는 다양한 곡을 고른다.
        """
        .formatted(joined);
  }

  /** Stage 2 시스템 프롬프트. */
  public String recommendSystemPrompt() {
    return """
        너는 분위기 description과 keyword_names가 주어졌을 때 어울리는 음악을 추천하는 큐레이터다.
        반드시 recommend_songs tool을 호출해 응답해야 한다.
        규칙:
        - excluded 목록의 곡(title+artist)은 절대 포함하지 않는다.
        - candidates: title/artist 쌍의 새 곡을 정확히 20개 제시한다.
        """;
  }

  /** Stage 1 유저 메시지: 이미지 URL + 텍스트. */
  public MessageParam analyzeUserMessage(String imageUrl) {
    ImageBlockParam imageBlock = ImageBlockParam.builder().urlSource(imageUrl).build();
    TextBlockParam textBlock = TextBlockParam.builder().text(ANALYZE_USER_TEXT).build();
    return MessageParam.builder()
        .role(MessageParam.Role.USER)
        .contentOfBlockParams(
            List.of(ContentBlockParam.ofImage(imageBlock), ContentBlockParam.ofText(textBlock)))
        .build();
  }

  /** Stage 2 유저 메시지: description/keywords/excluded JSON 직렬화. */
  public MessageParam recommendUserMessage(
      String description, List<String> keywordNames, List<SongCandidate> excludeTracks) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("description", description);
    payload.put("keyword_names", keywordNames);
    payload.put(
        "excluded",
        excludeTracks.stream().map(c -> Map.of("title", c.title(), "artist", c.artist())).toList());

    String json;
    try {
      json = objectMapper.writeValueAsString(payload);
    } catch (JacksonException e) {
      throw new BusinessException(ErrorCode.INTERNAL_ERROR, "추천 요청 직렬화 실패", null);
    }
    return MessageParam.builder().role(MessageParam.Role.USER).content(json).build();
  }
}
