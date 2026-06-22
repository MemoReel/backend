package com.memoreel.backend.recommendation;

import com.memoreel.backend.recommendation.port.LlmPort;
import com.memoreel.backend.recommendation.port.RecommendedTrack;
import com.memoreel.backend.recommendation.port.SongCandidate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 첫 후보 묶음을 iTunes로 매칭한 뒤, 매칭 성공한 트랙이 목표치(20곡)에 못 미치면 LLM에 새 후보를 더 요청해서 채운다.
 *
 * <p>매 호출마다 LLM은 정확히 20개 후보를 주는 것을 가정한다. 매칭에 성공한 트랙을 최대 {@link #TARGET_COUNT}개까지 그대로 모두 반환한다(프론트에서
 * 페이지 단위로 노출). 보충 요청은 최대 {@link #MAX_BACKFILL_ROUNDS}회까지만 하고, 그래도 부족하면 있는 만큼만 반환한다.
 */
@Component
public class TrackBackfillResolver {

  private static final int TARGET_COUNT = 20;
  private static final int MAX_BACKFILL_ROUNDS = 2;

  private final LlmPort llmPort;
  private final SongResolver songResolver;

  public TrackBackfillResolver(LlmPort llmPort, SongResolver songResolver) {
    this.llmPort = llmPort;
    this.songResolver = songResolver;
  }

  /**
   * @param initialCandidates 호출자가 이미 받아온 첫 라운드 후보 (Stage1 analyzePhoto 또는 Stage2 recommendSongs 결과)
   * @param alreadyExcluded 첫 라운드를 요청할 때 LLM에 이미 전달했던 제외 목록 (재추천 시 사용자가 이미 본 곡들)
   */
  public List<RecommendedTrack> resolve(
      String description,
      List<String> keywordNames,
      List<SongCandidate> initialCandidates,
      List<SongCandidate> alreadyExcluded) {
    List<SongCandidate> tried = new ArrayList<>(alreadyExcluded);
    tried.addAll(initialCandidates);

    Map<String, RecommendedTrack> matched = new LinkedHashMap<>();
    addAll(matched, songResolver.resolve(initialCandidates));

    int round = 0;
    while (matched.size() < TARGET_COUNT && round < MAX_BACKFILL_ROUNDS) {
      List<SongCandidate> more = llmPort.recommendSongs(description, keywordNames, tried);
      tried.addAll(more);
      addAll(matched, songResolver.resolve(more));
      round++;
    }

    return matched.values().stream().limit(TARGET_COUNT).toList();
  }

  private void addAll(Map<String, RecommendedTrack> matched, List<RecommendedTrack> tracks) {
    for (RecommendedTrack track : tracks) {
      matched.putIfAbsent(track.trackId(), track);
    }
  }
}
