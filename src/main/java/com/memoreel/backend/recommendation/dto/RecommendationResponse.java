package com.memoreel.backend.recommendation.dto;

import com.memoreel.backend.keyword.dto.KeywordRef;
import com.memoreel.backend.recommendation.port.Recommendation;
import java.util.List;

/** 추천 응답 페이로드 (명세 §3-1). */
public record RecommendationResponse(
    String photoUrl, List<KeywordRef> keywords, List<TrackResponse> tracks) {

  public static RecommendationResponse of(String photoUrl, Recommendation recommendation) {
    return new RecommendationResponse(
        photoUrl,
        recommendation.keywords().stream().map(KeywordRef::from).toList(),
        recommendation.tracks().stream().map(TrackResponse::from).toList());
  }
}
