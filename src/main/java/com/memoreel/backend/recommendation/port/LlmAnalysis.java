package com.memoreel.backend.recommendation.port;

import java.util.List;

/** LLM Stage 1(사진 분석) 결과. 사진 설명 + 매칭 키워드 이름 + 곡 후보 5개. */
public record LlmAnalysis(
    String description, List<String> keywordNames, List<SongCandidate> candidates) {}
