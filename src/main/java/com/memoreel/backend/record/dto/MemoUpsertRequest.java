package com.memoreel.backend.record.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** POST /records/{recordId}/memo 요청 페이로드. 메모 작성/수정 공용. */
public record MemoUpsertRequest(@NotBlank @Size(max = 500) String memoText) {}
