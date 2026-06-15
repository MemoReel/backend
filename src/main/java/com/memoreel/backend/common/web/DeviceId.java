package com.memoreel.backend.common.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터에 X-Device-Id 헤더 값을 주입한다 (명세 §0-3).
 * 헤더 누락 시 401 UNAUTHORIZED.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeviceId {
}
