package com.memoreel.backend.keyword.dto;

import com.memoreel.backend.entity.MemoRecord;
import com.memoreel.backend.entity.Song;
import com.memoreel.backend.song.ExternalLinks;
import java.time.LocalDateTime;

/**
 * 본인이 저장한 곡 1건 (명세 §5-2 items). track_id는 "itunes:&lt;itunes_track_id&gt;" 형식, saved_at은 저장한
 * record의 생성 시각.
 */
public record SavedTrackResponse(
    String trackId,
    String title,
    String artist,
    String artworkUrl,
    String previewUrl,
    ExternalLinks externalLinks,
    Long sourceRecordId,
    LocalDateTime savedAt) {

  public static SavedTrackResponse from(MemoRecord record) {
    Song song = record.getSong();
    return new SavedTrackResponse(
        "itunes:" + song.getItunesTrackId(),
        song.getTrackName(),
        song.getArtistName(),
        song.getArtworkUrl(),
        song.getPreviewUrl(),
        ExternalLinks.of(song.getTrackName(), song.getArtistName()),
        record.getId(),
        record.getCreatedAt());
  }
}
