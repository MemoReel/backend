package com.memoreel.backend.keyword.dto;

import java.util.List;

/**
 * GET /keywords 응답 페이로드 (명세 §5-1). data: { items: [...] }
 */
public record KeywordListData(List<KeywordSummaryResponse> items) {
}
