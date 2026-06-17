package com.memoreel.backend.recommendation;

import com.memoreel.backend.common.response.ApiResponse;
import com.memoreel.backend.common.web.DeviceId;
import com.memoreel.backend.recommendation.dto.RecommendationResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class RecommendationController {

  private final RecommendationService recommendationService;

  public RecommendationController(RecommendationService recommendationService) {
    this.recommendationService = recommendationService;
  }

  /** 사진 1장으로 매칭된 키워드와 5곡의 추천 트랙을 응답한다 (명세 §3-1). 저장은 별도 엔드포인트({@code POST /records})에서 수행한다. */
  @PostMapping(path = "/recommendations", consumes = "multipart/form-data")
  public ApiResponse<RecommendationResponse> recommend(
      @DeviceId String deviceId,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "taken_at", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime takenAt,
      @RequestParam(value = "lat", required = false) BigDecimal lat,
      @RequestParam(value = "lng", required = false) BigDecimal lng,
      @RequestParam(value = "exclude_track_ids", required = false) List<String> excludeTrackIds) {
    Set<String> exclude = excludeTrackIds == null ? Set.of() : Set.copyOf(excludeTrackIds);
    return ApiResponse.success(
        recommendationService.recommend(deviceId, file, takenAt, lat, lng, exclude));
  }
}
