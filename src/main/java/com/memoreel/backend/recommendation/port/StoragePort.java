package com.memoreel.backend.recommendation.port;

import org.springframework.web.multipart.MultipartFile;

/** 사진/파일 저장소 추상화. 어댑터: 로컬, S3, MinIO 등. */
public interface StoragePort {

  /**
   * 멀티파트 파일을 영속적인 위치에 저장하고 접근 가능한 URL을 반환한다.
   *
   * @param file 업로드된 파일 (jpeg/png/heic 가정)
   * @return 저장된 리소스의 절대 URL
   */
  String store(MultipartFile file);
}
