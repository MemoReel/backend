package com.memoreel.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing(@CreatedDate/@LastModifiedDate) 활성화.
 * 메인 클래스에서 분리하여, JPA를 띄우지 않는 슬라이스 테스트(@JsonTest 등)가 깨지지 않도록 한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
