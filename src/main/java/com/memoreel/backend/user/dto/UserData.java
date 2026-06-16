package com.memoreel.backend.user.dto;

import com.memoreel.backend.entity.User;

/**
 * {@code data: { user: {...} }} 래퍼 (명세 §1-1, §7-1).
 */
public record UserData(UserResponse user) {

    public static UserData from(User user) {
        return new UserData(UserResponse.from(user));
    }
}
