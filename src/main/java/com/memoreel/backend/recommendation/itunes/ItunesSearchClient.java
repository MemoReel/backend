package com.memoreel.backend.recommendation.itunes;

import com.memoreel.backend.recommendation.port.RecommendedTrack;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** iTunes Search API로 곡 제목+아티스트를 검색해 상위 1건을 가져온다. */
@Component
public class ItunesSearchClient {

  private static final Logger log = LoggerFactory.getLogger(ItunesSearchClient.class);
  private static final int PREVIEW_DURATION_SEC = 30;

  private final RestClient itunesRestClient;

  public ItunesSearchClient(RestClient itunesRestClient) {
    this.itunesRestClient = itunesRestClient;
  }

  /**
   * 제목+아티스트로 검색해 상위 1건을 {@link RecommendedTrack}으로 변환한다.
   *
   * @return 매칭된 트랙이 없거나 조회에 실패하면 {@link Optional#empty()}
   */
  public Optional<RecommendedTrack> searchTop(String title, String artist) {
    try {
      String term = title + " " + artist;
      ItunesSearchResponse response =
          itunesRestClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/search")
                          .queryParam("term", term)
                          .queryParam("media", "music")
                          .queryParam("entity", "song")
                          .queryParam("limit", 1)
                          .build())
              .retrieve()
              .body(ItunesSearchResponse.class);

      List<ItunesTrack> results = response == null ? List.of() : response.results();
      if (results.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(toRecommendedTrack(results.get(0)));
    } catch (RuntimeException e) {
      log.warn("iTunes 검색 실패: title={}, artist={}, message={}", title, artist, e.getMessage());
      return Optional.empty();
    }
  }

  private RecommendedTrack toRecommendedTrack(ItunesTrack track) {
    return new RecommendedTrack(
        "itunes:" + track.trackId(),
        track.trackName(),
        track.artistName(),
        track.artworkUrl100(),
        track.previewUrl(),
        PREVIEW_DURATION_SEC);
  }
}
