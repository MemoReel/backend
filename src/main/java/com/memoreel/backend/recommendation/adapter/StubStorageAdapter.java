package com.memoreel.backend.recommendation.adapter;

import com.memoreel.backend.recommendation.port.StoragePort;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 외부 스토리지 어댑터 도입 전 사용하는 기본 구현. 실제 업로드 없이 고정 prefix + UUID로 URL 문자열만 생성한다.
 *
 * <p>실제 S3/MinIO 어댑터 도입 시 {@code @Primary}로 덮어쓴다.
 */
@Component
public class StubStorageAdapter implements StoragePort {

  private static final String URL_PREFIX = "https://stub.memoreel.app/photos/";

  @Override
  public String store(MultipartFile file) {
    return URL_PREFIX + UUID.randomUUID() + extension(file.getOriginalFilename());
  }

  private String extension(String filename) {
    if (filename == null) {
      return ".jpg";
    }
    int dot = filename.lastIndexOf('.');
    return dot >= 0 ? filename.substring(dot) : ".jpg";
  }
}
