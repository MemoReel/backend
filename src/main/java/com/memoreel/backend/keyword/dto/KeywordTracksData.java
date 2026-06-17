package com.memoreel.backend.keyword.dto;

import java.util.List;

/**
 * GET /keywords/{keyword_id}/tracks 응답 페이로드 (명세 §5-2). data: { keyword: {...}, items: [...],
 * next_cursor: ... }
 */
public record KeywordTracksData(
    KeywordRef keyword, List<SavedTrackResponse> items, String nextCursor) {}
