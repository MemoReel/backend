package com.memoreel.backend.recommendation.itunes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** iTunes Search API 원시 응답의 단일 트랙. 필드명은 iTunes 응답 그대로(camelCase) 매핑한다. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ItunesTrack(
    long trackId, String trackName, String artistName, String artworkUrl100, String previewUrl) {}
