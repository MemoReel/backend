package com.memoreel.backend.recommendation;

import com.memoreel.backend.recommendation.itunes.ItunesSearchClient;
import com.memoreel.backend.recommendation.port.RecommendedTrack;
import com.memoreel.backend.recommendation.port.SongCandidate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * LLM이 제안한 곡 후보(제목/아티스트)를 iTunes Search로 실제 트랙에 매칭한다. 매칭 실패한 후보는 버린다.
 *
 * <p>LLM은 목표치(5곡)보다 많은 후보를 줘서 매칭 실패를 보충하므로, 최종 결과는 최대 {@link #TARGET_COUNT}개로 자른다. 매칭 성공한 후보가 그보다
 * 적으면 있는 만큼만 반환한다.
 */
@Component
public class SongResolver {

  private static final int TARGET_COUNT = 5;

  private final ItunesSearchClient itunesSearchClient;

  public SongResolver(ItunesSearchClient itunesSearchClient) {
    this.itunesSearchClient = itunesSearchClient;
  }

  public List<RecommendedTrack> resolve(List<SongCandidate> candidates) {
    return candidates.stream()
        .map(candidate -> itunesSearchClient.searchTop(candidate.title(), candidate.artist()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .limit(TARGET_COUNT)
        .toList();
  }
}
