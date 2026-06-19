package com.memoreel.backend.recommendation.adapter.claude;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Tool;
import java.util.List;
import java.util.Map;

/**
 * Claude tool use 강제를 위한 두 tool 스키마 정의 (스펙 §4).
 *
 * <ul>
 *   <li>{@link #ANALYZE_PHOTO_TOOL_NAME} - Stage 1: description + keyword_names + candidates(10개)
 *   <li>{@link #RECOMMEND_SONGS_TOOL_NAME} - Stage 2: candidates(10개)
 * </ul>
 */
public final class ClaudeToolSchemas {

  public static final String ANALYZE_PHOTO_TOOL_NAME = "analyze_photo";
  public static final String RECOMMEND_SONGS_TOOL_NAME = "recommend_songs";

  private static final int CANDIDATES_SIZE = 10;
  private static final int KEYWORDS_MIN = 1;
  private static final int KEYWORDS_MAX = 3;

  private static final Map<String, Object> STRING_TYPE = Map.of("type", "string");

  private static final Map<String, Object> CANDIDATE_ITEM_SCHEMA =
      Map.of(
          "type",
          "object",
          "required",
          List.of("title", "artist"),
          "properties",
          Map.of("title", STRING_TYPE, "artist", STRING_TYPE));

  private static final Map<String, Object> CANDIDATES_ARRAY_SCHEMA =
      Map.of(
          "type",
          "array",
          "minItems",
          CANDIDATES_SIZE,
          "maxItems",
          CANDIDATES_SIZE,
          "items",
          CANDIDATE_ITEM_SCHEMA);

  private static final Map<String, Object> KEYWORD_NAMES_ARRAY_SCHEMA =
      Map.of(
          "type",
          "array",
          "items",
          STRING_TYPE,
          "minItems",
          KEYWORDS_MIN,
          "maxItems",
          KEYWORDS_MAX);

  private ClaudeToolSchemas() {}

  public static Tool analyzePhotoTool() {
    Tool.InputSchema.Properties properties =
        Tool.InputSchema.Properties.builder()
            .putAdditionalProperty("description", JsonValue.from(STRING_TYPE))
            .putAdditionalProperty("keyword_names", JsonValue.from(KEYWORD_NAMES_ARRAY_SCHEMA))
            .putAdditionalProperty("candidates", JsonValue.from(CANDIDATES_ARRAY_SCHEMA))
            .build();
    Tool.InputSchema schema =
        Tool.InputSchema.builder()
            .properties(properties)
            .required(List.of("description", "keyword_names", "candidates"))
            .build();
    return Tool.builder()
        .name(ANALYZE_PHOTO_TOOL_NAME)
        .description("사진 분위기를 분석해 한국어 설명, 매칭 키워드(목록 내 1~3개), 곡 후보 10개를 반환한다.")
        .inputSchema(schema)
        .build();
  }

  public static Tool recommendSongsTool() {
    Tool.InputSchema.Properties properties =
        Tool.InputSchema.Properties.builder()
            .putAdditionalProperty("candidates", JsonValue.from(CANDIDATES_ARRAY_SCHEMA))
            .build();
    Tool.InputSchema schema =
        Tool.InputSchema.builder().properties(properties).required(List.of("candidates")).build();
    return Tool.builder()
        .name(RECOMMEND_SONGS_TOOL_NAME)
        .description("주어진 분위기/키워드/제외 곡을 바탕으로 새 곡 후보 10개를 반환한다.")
        .inputSchema(schema)
        .build();
  }
}
