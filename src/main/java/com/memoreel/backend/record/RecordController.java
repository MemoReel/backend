package com.memoreel.backend.record;

import com.memoreel.backend.common.response.ApiResponse;
import com.memoreel.backend.common.web.DeviceId;
import com.memoreel.backend.record.dto.RecordCreateRequest;
import com.memoreel.backend.record.dto.RecordResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecordController {

  private final RecordService recordService;

  public RecordController(RecordService recordService) {
    this.recordService = recordService;
  }

  /** 좋아요한 1곡과 사진을 record로 저장한다 (명세 §4-1). */
  @PostMapping("/records")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<RecordResponse> create(
      @DeviceId String deviceId, @Valid @RequestBody RecordCreateRequest request) {
    return ApiResponse.success(recordService.create(deviceId, request));
  }
}
