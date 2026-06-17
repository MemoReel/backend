package com.memoreel.backend.common.web;

import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link DeviceId} 파라미터에 X-Device-Id 헤더 값을 주입한다. 실제 user 매핑(미등록 기기 401)은 POST /users 이슈에서
 * UserRepository와 함께 완성된다.
 */
public class DeviceIdArgumentResolver implements HandlerMethodArgumentResolver {

  public static final String HEADER_NAME = "X-Device-Id";

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(DeviceId.class)
        && parameter.getParameterType().equals(String.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    String deviceId = webRequest.getHeader(HEADER_NAME);
    if (deviceId == null || deviceId.isBlank()) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    return deviceId;
  }
}
