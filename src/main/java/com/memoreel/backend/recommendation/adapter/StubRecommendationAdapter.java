package com.memoreel.backend.recommendation.adapter;

import com.memoreel.backend.entity.Keyword;
import com.memoreel.backend.keyword.KeywordRepository;
import com.memoreel.backend.recommendation.port.Recommendation;
import com.memoreel.backend.recommendation.port.RecommendationPort;
import com.memoreel.backend.recommendation.port.RecommendedTrack;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * LLM 추천 파이프라인 도입 전 사용하는 기본 구현. 매번 동일한 키워드(최대 3개)와 고정된 5개 트랙을 반환한다.
 *
 * <p>실제 어댑터 도입 시 {@code @Primary}로 덮어쓴다.
 */
@Component
public class StubRecommendationAdapter implements RecommendationPort {

  private static final int MAX_KEYWORDS = 3;

  private static final List<RecommendedTrack> FIXTURE_TRACKS =
      List.of(
          new RecommendedTrack(
              "itunes:1440857781",
              "Beach",
              "The Neighbourhood",
              "https://stub.memoreel.app/artwork/beach.jpg",
              "https://stub.memoreel.app/preview/beach.m4a",
              30),
          new RecommendedTrack(
              "itunes:1440857782",
              "Sweater Weather",
              "The Neighbourhood",
              "https://stub.memoreel.app/artwork/sweater-weather.jpg",
              "https://stub.memoreel.app/preview/sweater-weather.m4a",
              30),
          new RecommendedTrack(
              "itunes:1440857783",
              "Daylight",
              "Taylor Swift",
              "https://stub.memoreel.app/artwork/daylight.jpg",
              "https://stub.memoreel.app/preview/daylight.m4a",
              30),
          new RecommendedTrack(
              "itunes:1440857784",
              "Yellow",
              "Coldplay",
              "https://stub.memoreel.app/artwork/yellow.jpg",
              "https://stub.memoreel.app/preview/yellow.m4a",
              30),
          new RecommendedTrack(
              "itunes:1440857785",
              "Riptide",
              "Vance Joy",
              "https://stub.memoreel.app/artwork/riptide.jpg",
              "https://stub.memoreel.app/preview/riptide.m4a",
              30));

  private final KeywordRepository keywordRepository;

  public StubRecommendationAdapter(KeywordRepository keywordRepository) {
    this.keywordRepository = keywordRepository;
  }

  @Override
  public Recommendation recommend(
      String photoUrl,
      LocalDateTime takenAt,
      BigDecimal lat,
      BigDecimal lng,
      Set<String> excludeTrackIds) {
    List<Keyword> keywords = keywordRepository.findAll().stream().limit(MAX_KEYWORDS).toList();
    return new Recommendation(keywords, FIXTURE_TRACKS);
  }
}
