package com.memoreel.backend.recommendation;

import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import com.memoreel.backend.recommendation.dto.RecommendationResponse;
import com.memoreel.backend.recommendation.dto.RecommendationRetryRequest;
import com.memoreel.backend.recommendation.dto.RecommendationRetryResponse;
import com.memoreel.backend.recommendation.dto.TrackResponse;
import com.memoreel.backend.recommendation.port.LlmPort;
import com.memoreel.backend.recommendation.port.Recommendation;
import com.memoreel.backend.recommendation.port.RecommendationPort;
import com.memoreel.backend.recommendation.port.SongCandidate;
import com.memoreel.backend.recommendation.port.StoragePort;
import com.memoreel.backend.recommendation.port.StoredPhoto;
import com.memoreel.backend.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 사진 한 장으로 추천 5곡 + 매칭 키워드를 응답한다 (명세 §3-1). 이번 단계에서는 DB 저장이 없다. */
@Service
public class RecommendationService {

  private final UserRepository userRepository;
  private final StoragePort storagePort;
  private final RecommendationPort recommendationPort;
  private final LlmPort llmPort;
  private final TrackBackfillResolver trackBackfillResolver;

  public RecommendationService(
      UserRepository userRepository,
      StoragePort storagePort,
      RecommendationPort recommendationPort,
      LlmPort llmPort,
      TrackBackfillResolver trackBackfillResolver) {
    this.userRepository = userRepository;
    this.recommendationPort = recommendationPort;
    this.storagePort = storagePort;
    this.llmPort = llmPort;
    this.trackBackfillResolver = trackBackfillResolver;
  }

  public RecommendationResponse recommend(
      String deviceId, MultipartFile file, LocalDateTime takenAt, BigDecimal lat, BigDecimal lng) {
    requireRegisteredDevice(deviceId);
    requireFile(file);

    StoredPhoto photo = storagePort.storeTemp(file);
    Recommendation recommendation =
        recommendationPort.recommend(photo.photoUrl(), takenAt, lat, lng);
    return RecommendationResponse.of(photo, recommendation);
  }

  /** 사진 재분석 없이 description/keywords를 재사용해 다른 5곡을 추천한다 (명세 §3 재추천). */
  public RecommendationRetryResponse retry(String deviceId, RecommendationRetryRequest request) {
    requireRegisteredDevice(deviceId);

    List<SongCandidate> excludeTracks =
        request.excludeTracks() == null
            ? List.of()
            : request.excludeTracks().stream()
                .map(track -> new SongCandidate(track.title(), track.artist()))
                .toList();
    List<SongCandidate> candidates =
        llmPort.recommendSongs(request.description(), request.keywordNames(), excludeTracks);
    List<TrackResponse> tracks =
        trackBackfillResolver
            .resolve(request.description(), request.keywordNames(), candidates, excludeTracks)
            .stream()
            .map(TrackResponse::from)
            .toList();
    return new RecommendationRetryResponse(tracks);
  }

  private void requireRegisteredDevice(String deviceId) {
    if (deviceId == null
        || deviceId.isBlank()
        || userRepository.findByDeviceId(deviceId).isEmpty()) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
  }

  private void requireFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "file 파라미터가 비어있습니다.", null);
    }
  }
}
