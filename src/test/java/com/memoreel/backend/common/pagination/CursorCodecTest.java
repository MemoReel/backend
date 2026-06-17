package com.memoreel.backend.common.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CursorCodecTest {

  @Test
  void encode한_커서는_decode하면_원래_payload로_복원된다() {
    String payload = "createdAt|123";

    String cursor = CursorCodec.encode(payload);

    assertThat(cursor).isNotEqualTo(payload);
    assertThat(CursorCodec.decode(cursor)).isEqualTo(payload);
  }
}
