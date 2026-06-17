package com.memoreel.backend.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.memoreel.backend.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void BusinessException은_ErrorCode의_HTTP상태와_code로_매핑된다() {
    ResponseEntity<ApiResponse<Void>> response =
        handler.handleBusiness(new BusinessException(ErrorCode.NOT_FOUND));

    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isOk()).isFalse();
    assertThat(response.getBody().getError().getCode()).isEqualTo("NOT_FOUND");
  }

  @Test
  void 미처리_예외는_500_INTERNAL_ERROR로_매핑된다() {
    ResponseEntity<ApiResponse<Void>> response =
        handler.handleUnexpected(new RuntimeException("boom"));

    assertThat(response.getStatusCode().value()).isEqualTo(500);
    assertThat(response.getBody().getError().getCode()).isEqualTo("INTERNAL_ERROR");
  }
}
