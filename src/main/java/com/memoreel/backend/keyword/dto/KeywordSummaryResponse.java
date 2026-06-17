package com.memoreel.backend.keyword.dto;

/** 마스터 키워드 + 본인 저장 곡 수 (명세 §5-1). JSON: {"keyword_id", "name", "track_count"} */
public record KeywordSummaryResponse(Long keywordId, String name, long trackCount) {}
