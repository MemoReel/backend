package com.memoreel.backend.recommendation.dto;

import com.memoreel.backend.recommendation.port.RecommendedTrack;
import com.memoreel.backend.song.ExternalLinks;

/** 추천 트랙 응답 DTO (명세 §3-1). JSON snake_case는 Jackson 글로벌 설정에서 처리. */
public record TrackResponse(
    String trackId,
    String title,
    String artist,
    String artworkUrl,
    String previewUrl,
    int previewDurationSec,
    ExternalLinks externalLinks) {

  public static TrackResponse from(RecommendedTrack track) {
    return new TrackResponse(
        track.trackId(),
        track.title(),
        track.artist(),
        track.artworkUrl(),
        track.previewUrl(),
        track.previewDurationSec(),
        ExternalLinks.of(track.title(), track.artist()));
  }
}
