package com.memoreel.backend.recommendation.port;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/** 사진 기반 음악 추천 파이프라인 추상화. 어댑터: Stub, LLM 등. */
public interface RecommendationPort {

  /**
   * 사진 URL을 입력으로 매칭된 키워드와 5곡의 추천 트랙을 반환한다.
   *
   * @param photoUrl StoragePort가 반환한 URL
   * @param takenAt 사진 촬영 시각 (없으면 null)
   * @param lat 위도 (없으면 null)
   * @param lng 경도 (없으면 null)
   * @param excludeTrackIds 다시 추천 시 제외할 trackId 집합. 비어 있을 수 있다.
   * @return 키워드 + 5개의 추천 트랙
   */
  Recommendation recommend(
      String photoUrl,
      LocalDateTime takenAt,
      BigDecimal lat,
      BigDecimal lng,
      Set<String> excludeTrackIds);
}
