package com.memoreel.backend.record.dto;

import com.memoreel.backend.entity.Keyword;
import com.memoreel.backend.entity.MemoRecord;
import com.memoreel.backend.entity.Song;
import com.memoreel.backend.keyword.dto.KeywordRef;
import java.time.LocalDateTime;
import java.util.List;

/** GET /records 응답 페이로드 (명세 §4-2). MVP는 페이지네이션 없이 전체를 한 번에 반환한다. */
public record RecordListResponse(List<Item> items) {

  public record Item(
      Long recordId,
      LocalDateTime createdAt,
      String photoUrl,
      String photoViewUrl,
      Track track,
      List<KeywordRef> keywords,
      boolean hasLocation) {

    public static Item of(MemoRecord record, List<Keyword> keywords, String photoViewUrl) {
      return new Item(
          record.getId(),
          record.getCreatedAt(),
          record.getPhotoUrl(),
          photoViewUrl,
          Track.from(record.getSong()),
          keywords.stream().map(KeywordRef::from).toList(),
          record.getLocationLat() != null || record.getLocationLng() != null);
    }
  }

  /** 리스트 뷰용 곡 요약 (external_links 없음). */
  public record Track(String title, String artist, String artworkUrl, String previewUrl) {

    public static Track from(Song song) {
      return new Track(
          song.getTrackName(), song.getArtistName(), song.getArtworkUrl(), song.getPreviewUrl());
    }
  }
}
