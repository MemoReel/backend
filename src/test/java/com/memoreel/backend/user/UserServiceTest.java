package com.memoreel.backend.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.memoreel.backend.common.config.JpaAuditingConfig;
import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import com.memoreel.backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({UserService.class, JpaAuditingConfig.class})
class UserServiceTest {

  @Autowired private UserService userService;

  @Autowired private UserRepository userRepository;

  @Test
  void register_신규_기기를_저장하고_created_true를_반환한다() {
    RegisterResult result = userService.register("device-1", "지은");

    assertThat(result.created()).isTrue();
    assertThat(result.user().getId()).isNotNull();
    assertThat(result.user().getDeviceId()).isEqualTo("device-1");
    assertThat(result.user().getNickname()).isEqualTo("지은");
    assertThat(result.user().getCreatedAt()).isNotNull();
    assertThat(userRepository.findByDeviceId("device-1")).isPresent();
  }

  @Test
  void register_이미_등록된_기기는_기존_user를_그대로_반환하고_created_false다() {
    RegisterResult first = userService.register("device-1", "지은");
    RegisterResult again = userService.register("device-1", "다른닉네임");

    assertThat(again.created()).isFalse();
    assertThat(again.user().getId()).isEqualTo(first.user().getId());
    assertThat(again.user().getNickname()).isEqualTo("지은");
    assertThat(userRepository.count()).isEqualTo(1);
  }

  @Test
  void register_닉네임_앞뒤_공백을_제거한다() {
    RegisterResult result = userService.register("device-1", "  지은  ");

    assertThat(result.user().getNickname()).isEqualTo("지은");
  }

  @Test
  void register_닉네임이_없으면_user_id_기반_기본_닉네임을_부여한다() {
    RegisterResult result = userService.register("device-1", null);

    assertThat(result.created()).isTrue();
    assertThat(result.user().getNickname()).isEqualTo("사용자" + result.user().getId());
  }

  @Test
  void register_닉네임이_공백뿐이면_기본_닉네임을_부여한다() {
    RegisterResult result = userService.register("device-1", "   ");

    assertThat(result.user().getNickname()).isEqualTo("사용자" + result.user().getId());
  }

  @Test
  void getByDeviceId_등록된_기기의_user를_반환한다() {
    userService.register("device-1", "지은");

    User user = userService.getByDeviceId("device-1");

    assertThat(user.getDeviceId()).isEqualTo("device-1");
    assertThat(user.getNickname()).isEqualTo("지은");
  }

  @Test
  void getByDeviceId_미등록_기기는_UNAUTHORIZED_예외를_던진다() {
    assertThatThrownBy(() -> userService.getByDeviceId("unknown-device"))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.UNAUTHORIZED);
  }
}
