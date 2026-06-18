package com.memoreel.backend.recommendation.port;

/**
 * 저장소에 저장된 사진의 식별자와 화면 표시용 URL.
 *
 * @param photoUrl 왕복용 식별자(S3 key 등). 만료되지 않으며 retry/records 요청에 그대로 되돌려준다.
 * @param viewUrl 화면 표시용 URL(presigned 등). 만료 시간이 있을 수 있으므로 왕복 식별자로 사용하지 않는다.
 */
public record StoredPhoto(String photoUrl, String viewUrl) {}
