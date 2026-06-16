package com.memoreel.backend.song;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 곡 풀 재생용 외부 서비스 검색 링크 (명세 §3~§6, §9).
 * 제목+아티스트로 검색 URL 딥링크를 그때그때 조합한다 (DB 저장 X, 계정 연동 X).
 *
 * <p>⚠️ MVP 범위에서는 실제로 사용하지 않는다. 본 서비스는 추천(30초 preview)이 핵심이고
 * 풀 음악 재생은 지원하지 않는다. 풀 재생은 추후 Apple Music API 등으로 검토 예정이나,
 * 당장은 명세 응답 스펙을 맞추기 위해 필드만 채운다.
 */
public record ExternalLinks(String youtubeMusic, String spotify) {

    public static ExternalLinks of(String trackName, String artistName) {
        String query = URLEncoder.encode(trackName + " " + artistName, StandardCharsets.UTF_8);
        return new ExternalLinks(
                "https://music.youtube.com/search?q=" + query,
                "https://open.spotify.com/search/" + query
        );
    }
}
