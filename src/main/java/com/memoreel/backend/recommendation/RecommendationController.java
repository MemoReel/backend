package com.memoreel.backend.recommendation;

import com.memoreel.backend.common.response.ApiResponse;
import com.memoreel.backend.common.web.DeviceId;
import com.memoreel.backend.recommendation.dto.RecommendationResponse;
import com.memoreel.backend.recommendation.dto.RecommendationRetryRequest;
import com.memoreel.backend.recommendation.dto.RecommendationRetryResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class RecommendationController {

  private final RecommendationService recommendationService;

  public RecommendationController(RecommendationService recommendationService) {
    this.recommendationService = recommendationService;
  }

  /**
   * 사진 1장으로 매칭된 키워드와 추천 트랙(매칭된 만큼, 최대 10곡)을 응답한다 (명세 §3-1). 저장은 별도 엔드포인트({@code POST /records})에서
   * 수행한다.
   */
  @PostMapping(path = "/recommendations", consumes = "multipart/form-data")
  public ApiResponse<RecommendationResponse> recommend(
      @DeviceId String deviceId,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "taken_at", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime takenAt,
      @RequestParam(value = "lat", required = false) BigDecimal lat,
      @RequestParam(value = "lng", required = false) BigDecimal lng) {
    return ApiResponse.success(recommendationService.recommend(deviceId, file, takenAt, lat, lng));
  }

  /** 사진 재분석 없이 description/keywords를 재사용해 다른 트랙(매칭된 만큼, 최대 10곡)을 추천한다 (명세 §3 재추천). */
  @PostMapping("/recommendations/retry")
  public ApiResponse<RecommendationRetryResponse> retry(
      @DeviceId String deviceId, @Valid @RequestBody RecommendationRetryRequest request) {
    return ApiResponse.success(recommendationService.retry(deviceId, request));
  }
}
