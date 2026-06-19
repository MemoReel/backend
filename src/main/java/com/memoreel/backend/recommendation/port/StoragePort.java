package com.memoreel.backend.recommendation.port;

import org.springframework.web.multipart.MultipartFile;

/** 사진/파일 저장소 추상화. 2단계(temp -> permanent) 저장을 지원한다. 어댑터: S3, 로컬, MinIO 등. */
public interface StoragePort {

  /**
   * 멀티파트 파일을 임시 위치에 저장한다. 추천 응답 시점({@code POST /recommendations})에 호출된다.
   *
   * @param file 업로드된 파일 (jpeg/png/heic 가정)
   * @return 저장된 리소스의 식별자와 화면 표시용 URL
   */
  StoredPhoto storeTemp(MultipartFile file);

  /**
   * 임시 위치의 파일을 영구 위치로 이동한다. 사용자가 곡을 선택해 저장할 때({@code POST /records}) 호출된다.
   *
   * @param tempPhotoUrl {@link #storeTemp}가 반환한 {@link StoredPhoto#photoUrl()}
   * @return 영구 저장소로 이동된 리소스의 식별자와 화면 표시용 URL
   */
  StoredPhoto promote(String tempPhotoUrl);

  /**
   * 이미 저장된 리소스의 화면 표시용 URL을 새로 발급한다. 과거에 저장된 record를 조회할 때({@code GET /records}, {@code GET
   * /records/{id}}) 호출된다. 표시용 URL은 만료 시간이 있으므로 조회 시점마다 새로 발급해야 한다.
   *
   * @param photoUrl {@link #storeTemp} 또는 {@link #promote}가 반환한 식별자
   * @return 화면 표시용 URL
   */
  String viewUrl(String photoUrl);
}
