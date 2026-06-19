package com.memoreel.backend.recommendation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * POST /recommendations/retry 요청 페이로드. 사진 재분석 없이 직전 추천 응답의 description/keywords를 그대로 재전송하고, 이미 본
 * 곡(excludeTracks)을 제외한 새 추천을 받는다.
 */
public record RecommendationRetryRequest(
    String photoUrl,
    @NotBlank String description,
    @NotEmpty List<String> keywordNames,
    @Valid List<ExcludeTrack> excludeTracks) {

  /** 제외할 곡. trackId가 아닌 제목/아티스트 쌍으로 LLM에 전달된다. */
  public record ExcludeTrack(@NotBlank String title, @NotBlank String artist) {}
}
