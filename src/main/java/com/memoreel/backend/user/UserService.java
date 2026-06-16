package com.memoreel.backend.user;

import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import com.memoreel.backend.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 기기 최초 등록. 이미 등록된 기기면 기존 user를 그대로 반환한다(멱등, 명세 §1-1).
     * 닉네임을 생략하면 저장 후 생성된 id 기반 기본 닉네임("사용자{id}")을 부여한다.
     */
    @Transactional
    public RegisterResult register(String deviceId, String nickname) {
        return userRepository.findByDeviceId(deviceId)
                .map(user -> new RegisterResult(user, false))
                .orElseGet(() -> new RegisterResult(create(deviceId, nickname), true));
    }

    private User create(String deviceId, String nickname) {
        String trimmed = (nickname == null) ? null : nickname.trim();
        boolean useDefault = (trimmed == null || trimmed.isEmpty());

        User user = userRepository.save(
                User.builder()
                        .deviceId(deviceId)
                        .nickname(useDefault ? "" : trimmed)
                        .build());

        if (useDefault) {
            user.assignDefaultNickname("사용자" + user.getId());
        }
        return user;
    }

    /**
     * X-Device-Id로 식별된 user를 조회한다. 미등록 기기면 401 (명세 §0-3).
     */
    @Transactional(readOnly = true)
    public User getByDeviceId(String deviceId) {
        return userRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }
}
