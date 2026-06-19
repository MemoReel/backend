package com.memoreel.backend.recommendation.itunes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** iTunes Search API 원시 응답 (https://itunes.apple.com/search). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ItunesSearchResponse(int resultCount, List<ItunesTrack> results) {}
