package com.memoreel.backend.common.error;

import com.memoreel.backend.common.response.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
    return build(e.getErrorCode(), e.getMessage(), e.getDetails());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError error : e.getBindingResult().getFieldErrors()) {
      fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
    }
    return build(
        ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.getDefaultMessage(), fieldErrors);
  }

  @ExceptionHandler({
    MissingServletRequestPartException.class,
    MissingServletRequestParameterException.class
  })
  public ResponseEntity<ApiResponse<Void>> handleMissingRequestPart(Exception e) {
    return build(ErrorCode.VALIDATION_ERROR, e.getMessage(), null);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiResponse<Void>> handleUploadTooLarge(MaxUploadSizeExceededException e) {
    return build(
        ErrorCode.PAYLOAD_TOO_LARGE, ErrorCode.PAYLOAD_TOO_LARGE.getDefaultMessage(), null);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleMediaType(HttpMediaTypeNotSupportedException e) {
    return build(
        ErrorCode.UNSUPPORTED_MEDIA, ErrorCode.UNSUPPORTED_MEDIA.getDefaultMessage(), null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
    log.error("처리되지 않은 예외", e);
    return build(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage(), null);
  }

  private ResponseEntity<ApiResponse<Void>> build(ErrorCode code, String message, Object details) {
    return ResponseEntity.status(code.getStatus())
        .body(ApiResponse.error(code.name(), message, details));
  }
}
