package com.memoreel.backend.common.web;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터에 X-Device-Id 헤더 값을 주입한다 (명세 §0-3). 헤더 누락 시 401 UNAUTHORIZED.
 *
 * <p>OpenAPI(Swagger) 문서에는 {@code X-Device-Id} 필수 헤더 파라미터로 노출된다 (커스텀 ArgumentResolver라 springdoc가
 * 기본으로는 query 파라미터로 잘못 추론하므로 명시).
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(
    in = ParameterIn.HEADER,
    name = "X-Device-Id",
    required = true,
    description = "기기 고유 식별자 (명세 §0-3)",
    schema = @Schema(type = "string"))
public @interface DeviceId {}
