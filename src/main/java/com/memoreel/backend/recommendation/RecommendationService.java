package com.memoreel.backend.recommendation;

import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import com.memoreel.backend.recommendation.dto.RecommendationResponse;
import com.memoreel.backend.recommendation.port.Recommendation;
import com.memoreel.backend.recommendation.port.RecommendationPort;
import com.memoreel.backend.recommendation.port.StoragePort;
import com.memoreel.backend.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 사진 한 장으로 추천 5곡 + 매칭 키워드를 응답한다 (명세 §3-1). 이번 단계에서는 DB 저장이 없다. */
@Service
public class RecommendationService {

  private final UserRepository userRepository;
  private final StoragePort storagePort;
  private final RecommendationPort recommendationPort;

  public RecommendationService(
      UserRepository userRepository,
      StoragePort storagePort,
      RecommendationPort recommendationPort) {
    this.userRepository = userRepository;
    this.recommendationPort = recommendationPort;
    this.storagePort = storagePort;
  }

  public RecommendationResponse recommend(
      String deviceId,
      MultipartFile file,
      LocalDateTime takenAt,
      BigDecimal lat,
      BigDecimal lng,
      Set<String> excludeTrackIds) {
    requireRegisteredDevice(deviceId);
    requireFile(file);

    String photoUrl = storagePort.store(file);
    Recommendation recommendation =
        recommendationPort.recommend(photoUrl, takenAt, lat, lng, excludeTrackIds);
    return RecommendationResponse.of(photoUrl, recommendation);
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
