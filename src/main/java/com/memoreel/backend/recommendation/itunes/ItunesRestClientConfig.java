package com.memoreel.backend.recommendation.itunes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

/**
 * iTunes Search API 전용 RestClient 설정.
 *
 * <p>전역 Jackson 설정({@code spring.jackson.property-naming-strategy: SNAKE_CASE})은 우리 API 응답용이며,
 * iTunes가 내려주는 camelCase 필드(trackId, artistName 등)와 충돌한다. 따라서 naming strategy를 적용하지 않은 별도의
 * JsonMapper를 사용한다.
 */
@Configuration
public class ItunesRestClientConfig {

  private static final String BASE_URL = "https://itunes.apple.com";

  @Bean
  public RestClient itunesRestClient() {
    JsonMapper plainJsonMapper = JsonMapper.builder().build();
    return RestClient.builder()
        .baseUrl(BASE_URL)
        .configureMessageConverters(
            builder ->
                builder.withJsonConverter(new JacksonJsonHttpMessageConverter(plainJsonMapper)))
        .build();
  }
}
