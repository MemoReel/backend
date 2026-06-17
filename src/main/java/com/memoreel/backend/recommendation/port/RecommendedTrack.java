package com.memoreel.backend.recommendation.port;

/**
 * 추천 어댑터가 반환하는 단일 트랙의 원시 데이터. 외부 검색 링크는 응답 변환 시점에 조합한다.
 *
 * @param trackId "itunes:&lt;itunesTrackId&gt;" 형식
 */
public record RecommendedTrack(
    String trackId,
    String title,
    String artist,
    String artworkUrl,
    String previewUrl,
    int previewDurationSec) {}
