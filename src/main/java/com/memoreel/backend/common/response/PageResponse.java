package com.memoreel.backend.common.response;

import java.util.List;
import lombok.Getter;

/** 커서 기반 페이지네이션 응답 (명세 §0-6). {"items": [...], "next_cursor": "..."} (nextCursor=null이면 끝) */
@Getter
public class PageResponse<T> {

  private final List<T> items;
  private final String nextCursor;

  private PageResponse(List<T> items, String nextCursor) {
    this.items = items;
    this.nextCursor = nextCursor;
  }

  public static <T> PageResponse<T> of(List<T> items, String nextCursor) {
    return new PageResponse<>(items, nextCursor);
  }
}
