package com.memoreel.backend.record.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/** POST /records 요청 페이로드 (명세 §4-1). */
public record RecordCreateRequest(
    @NotBlank String photoUrl,
    @Valid @NotNull Track track,
    @NotNull @Size(min = 1, max = 3) List<Long> keywordIds,
    @Valid Location location) {

  /** 클라이언트가 전달하는 곡 메타 (추천 응답을 그대로 되돌려준다). */
  public record Track(
      @NotBlank String trackId,
      @NotBlank String title,
      @NotBlank String artist,
      String artworkUrl,
      String previewUrl) {}

  /** 위치 정보. location 객체를 보내면 좌표는 필수, 라벨은 선택. */
  public record Location(@NotNull BigDecimal lat, @NotNull BigDecimal lng, String label) {}
}
