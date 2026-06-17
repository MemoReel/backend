package com.memoreel.backend.common.error;

import lombok.Getter;

/** 도메인/비즈니스 예외. ErrorCode를 담아 GlobalExceptionHandler가 응답으로 변환한다. */
@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;
  private final transient Object details;

  public BusinessException(ErrorCode errorCode) {
    this(errorCode, errorCode.getDefaultMessage(), null);
  }

  public BusinessException(ErrorCode errorCode, String message) {
    this(errorCode, message, null);
  }

  public BusinessException(ErrorCode errorCode, String message, Object details) {
    super(message);
    this.errorCode = errorCode;
    this.details = details;
  }
}
