package com.memoreel.backend.keyword;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 운영진이 정의한 고정 마스터 키워드 셋 (명세 §6-5). 코드가 진실의 원천이며, {@link MasterKeywordSeeder}가 부팅 시점에 누락된 이름만 DB에
 * INSERT 한다. 이름 변경은 새 row를 만들고 기존 row는 그대로 둔다.
 */
public final class MasterKeywordCatalog {

  /** 정의 순서 그대로 유지. */
  public static final Set<String> NAMES =
      Collections.unmodifiableSet(
          new LinkedHashSet<>(
              java.util.List.of(
                  "노을", "비", "새벽", "카페", "드라이브", "잔잔함", "신남", "운동", "잠들기 전", "여행", "출근길", "감성", "봄",
                  "여름", "가을", "겨울", "눈", "벚꽃", "바다", "산책", "공부", "휴식", "파티", "혼자", "친구", "사랑", "이별",
                  "그리움", "설렘", "위로")));

  private MasterKeywordCatalog() {}
}
