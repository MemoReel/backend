package com.memoreel.backend.user.dto;

import com.memoreel.backend.entity.Gender;
import com.memoreel.backend.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 사용자 응답 객체. data.user 페이로드 (명세 §1-1, §7-1). */
public record UserResponse(
    Long id, String nickname, LocalDate birthDate, Gender gender, LocalDateTime createdAt) {

  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getNickname(),
        user.getBirthDate(),
        user.getGender(),
        user.getCreatedAt());
  }
}
