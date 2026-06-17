package com.memoreel.backend.keyword;

import com.memoreel.backend.common.response.ApiResponse;
import com.memoreel.backend.common.web.DeviceId;
import com.memoreel.backend.keyword.dto.KeywordListData;
import com.memoreel.backend.keyword.dto.KeywordTracksData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KeywordController {

    /** 명세 §0-6: 기본 20, 최대 50. */
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final KeywordService keywordService;

    public KeywordController(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    /**
     * 마스터 키워드 전체 + 본인 저장 곡 수 (명세 §5-1).
     */
    @GetMapping("/keywords")
    public ApiResponse<KeywordListData> getKeywords(@DeviceId String deviceId) {
        return ApiResponse.success(new KeywordListData(keywordService.getKeywords(deviceId)));
    }

    /**
     * 특정 키워드로 분류된 본인 저장 곡 목록 (명세 §5-2).
     */
    @GetMapping("/keywords/{keywordId}/tracks")
    public ApiResponse<KeywordTracksData> getKeywordTracks(
            @DeviceId String deviceId,
            @PathVariable Long keywordId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {
        int capped = Math.min(Math.max(limit, 1), MAX_LIMIT);
        return ApiResponse.success(keywordService.getTracks(deviceId, keywordId, cursor, capped));
    }
}
