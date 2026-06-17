package com.memoreel.backend.common.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

class DeviceIdArgumentResolverTest {

  private final DeviceIdArgumentResolver resolver = new DeviceIdArgumentResolver();

  @Test
  void X_Device_Id_헤더가_있으면_값을_반환한다() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(DeviceIdArgumentResolver.HEADER_NAME, "device-123");

    Object result = resolver.resolveArgument(null, null, new ServletWebRequest(request), null);

    assertThat(result).isEqualTo("device-123");
  }

  @Test
  void 헤더가_없으면_UNAUTHORIZED_예외를_던진다() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    assertThatThrownBy(
            () -> resolver.resolveArgument(null, null, new ServletWebRequest(request), null))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.UNAUTHORIZED);
  }
}
