package com.memoreel.backend.recommendation.dto;

import com.memoreel.backend.keyword.dto.KeywordRef;
import com.memoreel.backend.recommendation.port.Recommendation;
import com.memoreel.backend.recommendation.port.StoredPhoto;
import java.util.List;

/**
 * 추천 응답 페이로드 (명세 §3-1). 재추천({@code POST /recommendations/retry}) 요청 시 그대로 되돌려줄 수 있도록
 * description/keywords를 포함한다.
 */
public record RecommendationResponse(
    String photoUrl,
    String photoViewUrl,
    String description,
    List<KeywordRef> keywords,
    List<TrackResponse> tracks) {

  public static RecommendationResponse of(StoredPhoto photo, Recommendation recommendation) {
    return new RecommendationResponse(
        photo.photoUrl(),
        photo.viewUrl(),
        recommendation.description(),
        recommendation.keywords().stream().map(KeywordRef::from).toList(),
        recommendation.tracks().stream().map(TrackResponse::from).toList());
  }
}
