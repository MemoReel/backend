package com.memoreel.backend.keyword.dto;

import com.memoreel.backend.entity.Keyword;

/**
 * 키워드 참조 객체 (명세 §5-2 응답의 keyword 필드). JSON: {"keyword_id", "name"}
 */
public record KeywordRef(Long keywordId, String name) {

    public static KeywordRef from(Keyword keyword) {
        return new KeywordRef(keyword.getId(), keyword.getName());
    }
}
