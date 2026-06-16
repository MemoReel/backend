package com.memoreel.backend.user.dto;

import jakarta.validation.constraints.Size;

/**
 * POST /users 요청 본문 (명세 §1-1).
 * 닉네임은 선택이며, 생략하면 서버가 기본 닉네임을 부여한다. 값을 주면 2~20자.
 */
public record UserRegisterRequest(

        @Size(min = 2, max = 20)
        String nickname
) {
}
