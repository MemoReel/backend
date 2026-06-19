package com.memoreel.backend.recommendation.adapter;

import com.memoreel.backend.entity.Keyword;
import com.memoreel.backend.keyword.KeywordRepository;
import com.memoreel.backend.recommendation.TrackBackfillResolver;
import com.memoreel.backend.recommendation.port.LlmAnalysis;
import com.memoreel.backend.recommendation.port.LlmPort;
import com.memoreel.backend.recommendation.port.Recommendation;
import com.memoreel.backend.recommendation.port.RecommendationPort;
import com.memoreel.backend.recommendation.port.RecommendedTrack;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

/** LLM Stage 1(사진 분석)과 iTunes Search 매칭을 조합해 추천을 생성한다 (명세 §3-1). */
@Component
public class LlmRecommendationAdapter implements RecommendationPort {

  private static final int MAX_KEYWORDS = 3;

  private final LlmPort llmPort;
  private final KeywordRepository keywordRepository;
  private final TrackBackfillResolver trackBackfillResolver;

  public LlmRecommendationAdapter(
      LlmPort llmPort,
      KeywordRepository keywordRepository,
      TrackBackfillResolver trackBackfillResolver) {
    this.llmPort = llmPort;
    this.keywordRepository = keywordRepository;
    this.trackBackfillResolver = trackBackfillResolver;
  }

  @Override
  public Recommendation recommend(
      String photoUrl, LocalDateTime takenAt, BigDecimal lat, BigDecimal lng) {
    LlmAnalysis analysis = llmPort.analyzePhoto(photoUrl);
    List<String> keywordNames = analysis.keywordNames().stream().limit(MAX_KEYWORDS).toList();
    List<Keyword> keywords = keywordRepository.findByNameIn(keywordNames);
    List<RecommendedTrack> tracks =
        trackBackfillResolver.resolve(
            analysis.description(), keywordNames, analysis.candidates(), List.of());
    return new Recommendation(analysis.description(), keywords, tracks);
  }
}
