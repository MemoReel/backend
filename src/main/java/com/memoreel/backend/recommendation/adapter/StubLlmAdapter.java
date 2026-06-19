package com.memoreel.backend.recommendation.adapter;

import com.memoreel.backend.recommendation.port.LlmAnalysis;
import com.memoreel.backend.recommendation.port.LlmPort;
import com.memoreel.backend.recommendation.port.SongCandidate;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 고정된 결과를 반환하는 기본 구현. {@code memoreel.llm.provider=stub}(기본값)일 때 등록되어 운영 환경 이외에서 사용된다. 운영(prod
 * 등)에서는 {@code provider=claude}로 전환하여 {@link ClaudeLlmAdapter}가 대신 등록된다.
 */
@Component
@ConditionalOnProperty(name = "memoreel.llm.provider", havingValue = "stub", matchIfMissing = true)
public class StubLlmAdapter implements LlmPort {

  private static final List<String> FIXED_KEYWORDS = List.of("노을", "여행", "잔잔함");

  private static final List<SongCandidate> STAGE1_CANDIDATES =
      List.of(
          new SongCandidate("Beach", "The Neighbourhood"),
          new SongCandidate("Sweater Weather", "The Neighbourhood"),
          new SongCandidate("Daylight", "Taylor Swift"),
          new SongCandidate("Yellow", "Coldplay"),
          new SongCandidate("Riptide", "Vance Joy"),
          new SongCandidate("Sunset Lover", "Petit Biscuit"),
          new SongCandidate("Ocean Eyes", "Billie Eilish"),
          new SongCandidate("Slow Dancing in the Dark", "Joji"),
          new SongCandidate("Sunday Best", "Surfaces"),
          new SongCandidate("Cherry Wine", "Hozier"));

  private static final List<SongCandidate> STAGE2_CANDIDATES =
      List.of(
          new SongCandidate("Sunflower", "Post Malone"),
          new SongCandidate("Stay", "The Kid LAROI"),
          new SongCandidate("Sunroof", "Nicky Youre"),
          new SongCandidate("Adventure of a Lifetime", "Coldplay"),
          new SongCandidate("Electric Feel", "MGMT"),
          new SongCandidate("Best Part", "Daniel Caesar"),
          new SongCandidate("Liability", "Lorde"),
          new SongCandidate("Falling", "Harry Styles"),
          new SongCandidate("Lover, You Should've Come Over", "Jeff Buckley"),
          new SongCandidate("Holocene", "Bon Iver"));

  @Override
  public LlmAnalysis analyzePhoto(String photoUrl) {
    return new LlmAnalysis("노을 지는 바다 위로 갈매기가 날아가는 풍경", FIXED_KEYWORDS, STAGE1_CANDIDATES);
  }

  @Override
  public List<SongCandidate> recommendSongs(
      String description, List<String> keywordNames, List<SongCandidate> excludeTracks) {
    return STAGE2_CANDIDATES.stream()
        .filter(candidate -> !excludeTracks.contains(candidate))
        .toList();
  }
}
