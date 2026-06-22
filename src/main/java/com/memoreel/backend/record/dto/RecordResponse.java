package com.memoreel.backend.record.dto;

import com.memoreel.backend.entity.Keyword;
import com.memoreel.backend.entity.MemoRecord;
import com.memoreel.backend.entity.Song;
import com.memoreel.backend.keyword.dto.KeywordRef;
import com.memoreel.backend.song.ExternalLinks;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** POST /records 응답 페이로드 (명세 §4-1). */
public record RecordResponse(
    Long recordId,
    LocalDateTime createdAt,
    String photoUrl,
    String photoViewUrl,
    Track track,
    List<KeywordRef> keywords,
    Location location,
    String memoText) {

  public static RecordResponse of(MemoRecord record, List<Keyword> keywords, String photoViewUrl) {
    return new RecordResponse(
        record.getId(),
        record.getCreatedAt(),
        record.getPhotoUrl(),
        photoViewUrl,
        Track.from(record.getSong()),
        keywords.stream().map(KeywordRef::from).toList(),
        Location.from(record),
        record.getMemoText());
  }

  /** 응답에 포함되는 곡 DTO. */
  public record Track(
      String trackId,
      String title,
      String artist,
      String artworkUrl,
      String previewUrl,
      ExternalLinks externalLinks) {

    public static Track from(Song song) {
      return new Track(
          "itunes:" + song.getItunesTrackId(),
          song.getTrackName(),
          song.getArtistName(),
          song.getArtworkUrl(),
          song.getPreviewUrl(),
          ExternalLinks.of(song.getTrackName(), song.getArtistName()));
    }
  }

  /** 응답의 위치 DTO. 좌표가 둘 다 null이면 응답에서 location 자체가 null로 빠진다. */
  public record Location(BigDecimal lat, BigDecimal lng, String label) {

    public static Location from(MemoRecord record) {
      if (record.getLocationLat() == null && record.getLocationLng() == null) {
        return null;
      }
      return new Location(
          record.getLocationLat(), record.getLocationLng(), record.getLocationLabel());
    }
  }
}
