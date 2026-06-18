package com.memoreel.backend.recommendation.dto;

import java.util.List;

/**
 * POST /recommendations/retry 응답 페이로드. photo_url/description/keywords는 클라이언트가 이미 보유하므로 재반환하지 않는다.
 */
public record RecommendationRetryResponse(List<TrackResponse> tracks) {}
