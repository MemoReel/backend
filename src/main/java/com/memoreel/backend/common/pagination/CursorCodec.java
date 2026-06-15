package com.memoreel.backend.common.pagination;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 커서 페이지네이션용 불투명(opaque) 커서 인코딩/디코딩 (명세 §0-6).
 * 내부 payload 포맷은 도메인이 정하며(예: "id" 또는 "createdAt|id"), 여기서는 Base64로 감싸기만 한다.
 */
public final class CursorCodec {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private CursorCodec() {
    }

    public static String encode(String payload) {
        return ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String cursor) {
        return new String(DECODER.decode(cursor), StandardCharsets.UTF_8);
    }
}
