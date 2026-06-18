package com.memoreel.backend.user.dto;

import com.memoreel.backend.entity.Gender;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** POST /users 요청 본문 (명세 §1-1). 닉네임은 선택이며, 생략하면 서버가 기본 닉네임을 부여한다. 값을 주면 2~20자. 생년월일/성별은 필수. */
public record UserRegisterRequest(
    @Size(min = 2, max = 20) String nickname,
    @NotNull LocalDate birthDate,
    @NotNull Gender gender) {}
