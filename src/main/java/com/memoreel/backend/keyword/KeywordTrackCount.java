package com.memoreel.backend.keyword;

/**
 * 키워드별 저장 곡 수 집계 projection (명세 §5-1 track_count).
 */
public interface KeywordTrackCount {

    Long getKeywordId();

    long getTrackCount();
}
