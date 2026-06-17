package com.memoreel.backend.recommendation.port;

import com.memoreel.backend.entity.Keyword;
import java.util.List;

/** 추천 결과 도메인 객체. 매칭된 마스터 키워드와 5곡의 추천 트랙을 담는다. */
public record Recommendation(List<Keyword> keywords, List<RecommendedTrack> tracks) {}
