package com.memoreel.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, length = 128, unique = true)
    private String deviceId;

    @Column(nullable = false, length = 40)
    private String nickname;

    @Builder
    public User(String deviceId, String nickname) {
        this.deviceId = deviceId;
        this.nickname = nickname;
    }

    /**
     * 닉네임 없이 가입한 경우, 저장 후 생성된 id 기반 기본 닉네임을 부여한다.
     */
    public void assignDefaultNickname(String nickname) {
        this.nickname = nickname;
    }
}
