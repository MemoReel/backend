package com.memoreel.backend.common.response;

import lombok.Getter;

/**
 * 공통 응답 래퍼 (명세 §0-4).
 * 성공: {"ok": true, "data": {...}}
 * 실패: {"ok": false, "error": {"code", "message", "details"}}
 */
@Getter
public class ApiResponse<T> {

    private final boolean ok;
    private final T data;
    private final ErrorBody error;

    private ApiResponse(boolean ok, T data, ErrorBody error) {
        this.ok = ok;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> error(String code, String message, Object details) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message, details));
    }

    @Getter
    public static class ErrorBody {
        private final String code;
        private final String message;
        private final Object details;

        public ErrorBody(String code, String message, Object details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }
    }
}
