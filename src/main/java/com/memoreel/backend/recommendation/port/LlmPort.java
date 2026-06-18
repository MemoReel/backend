package com.memoreel.backend.recommendation.port;

import java.util.List;

/** 사진/키워드 기반 곡 후보 생성 LLM 파이프라인 추상화. 어댑터: Stub, Claude 등. */
public interface LlmPort {

  /** Stage 1: 사진을 분석해 설명, 매칭 키워드, 곡 후보 5개를 생성한다. */
  LlmAnalysis analyzePhoto(String photoUrl);

  /** Stage 2: 사진 설명+키워드로 곡 후보 5개를 재생성한다. excludeTracks는 이전에 추천했던 곡(제외 대상)이다. */
  List<SongCandidate> recommendSongs(
      String description, List<String> keywordNames, List<SongCandidate> excludeTracks);
}
