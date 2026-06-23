# MemoReel Backend

음악 기억(메모릴)을 기록하고, 키워드 기반으로 곡을 추천하는 서비스의 백엔드 API.

## 기술 스택

- Java 21 / Spring Boot 3
- JPA (Hibernate) / MySQL
- Docker Compose
- Gradle

## 도메인

- `user` — 디바이스 식별자 기반 사용자
- `record` — 메모릴(기억 한 단위)
- `song` — 곡 메타데이터
- `keyword` — 메모릴 키워드
- 추천 — 키워드로 곡 후보 매칭

## 로컬 실행

```bash
./gradlew bootRun
```

## 빌드/검증

```bash
./gradlew harness       # spotless + ArchUnit + test
./gradlew spotlessApply # 자동 포맷
```

자세한 컨벤션은 `CLAUDE.md`, 운영 셋업은 `docs/runbooks/` 참고.
