package com.memoreel.backend.user;

import com.memoreel.backend.entity.User;

/** 기기 등록 결과. {@code created}가 true면 신규 등록(201), false면 기존 기기 멱등 응답(200). */
public record RegisterResult(User user, boolean created) {}
