package com.memoreel.backend.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

@JsonTest
class ApiResponseTest {

  @Autowired private ObjectMapper objectMapper;

  @Test
  void 성공_응답은_ok_true와_data를_가지고_error는_생략된다() throws Exception {
    String json = objectMapper.writeValueAsString(ApiResponse.success(Map.of("value", 1)));

    assertThat(json).contains("\"ok\":true");
    assertThat(json).contains("\"data\"");
    assertThat(json).doesNotContain("error");
  }

  @Test
  void 실패_응답은_ok_false와_error를_가지고_data는_생략된다() throws Exception {
    String json =
        objectMapper.writeValueAsString(ApiResponse.error("VALIDATION_ERROR", "요청 검증 실패", null));

    assertThat(json).contains("\"ok\":false");
    assertThat(json).contains("VALIDATION_ERROR");
    assertThat(json).doesNotContain("\"data\"");
    assertThat(json).doesNotContain("details");
  }

  @Test
  void 응답_필드는_snake_case로_직렬화된다() throws Exception {
    String json = objectMapper.writeValueAsString(PageResponse.of(List.of(1, 2), "cur"));

    assertThat(json).contains("next_cursor");
    assertThat(json).doesNotContain("nextCursor");
  }
}
