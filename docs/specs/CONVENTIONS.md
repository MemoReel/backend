# CONVENTIONS

이 프로젝트의 코딩·도메인·하네스 컨벤션을 정리한다. `CLAUDE.md`가 요약이고, 본 문서가 상세본이다.

## 1. 패키지 / 아키텍처

- 베이스 패키지: `com.memoreel.backend`
- 도메인 디렉터리(`user/`, `song/`, `keyword/` 등) 안에 `*Controller`, `*Service`, `*Repository`, `dto/`를 둔다
- 공통 엔티티는 현재 `entity/`로 평탄화. 도메인이 커지면 `{도메인}/entity/`로 점진 이동 가능
- 공통 모듈은 `common/`: `config/`, `error/`, `response/`, `web/`, `pagination/`

### 의존성 규칙 (ArchUnit으로 강제)

- `*Controller` → `*Repository` **직접 호출 금지** (Service 경유)
- 패키지 간 **순환 의존성 금지** (slices)
- 클래스 위치: `*Controller`/`*Service`/`*Repository`는 각각 `..controller..`/`..service..`/`..repository..` 또는 도메인 루트 패키지

규칙 정의: `src/test/java/com/memoreel/backend/architecture/ArchitectureRules.java`

## 2. 코딩 스타일 (Spotless + google-java-format)

- 2-space 인덴트, import 정리, 줄 끝 공백 제거, 파일 끝 newline
- 미사용 import 자동 제거
- 적용: `./gradlew spotlessApply` (검증: `spotlessCheck`)
- 모든 커밋 전 pre-commit hook(`spotlessCheck`)이 자동 실행

## 3. API 응답

- 공통 래퍼: `com.memoreel.backend.common.response.ApiResponse`
  - 성공: `ApiResponse.success(data)` → `{ "ok": true, "data": ... }`
  - 실패: `GlobalExceptionHandler`가 자동 변환 → `{ "ok": false, "error": { code, message, details } }`
- Jackson: `SNAKE_CASE` + `default-property-inclusion: non_null` (`application.yml`)
- 상태코드: 생성은 `@ResponseStatus(HttpStatus.CREATED)`, 삭제는 `204 No Content`

### 에러 코드

`com.memoreel.backend.common.error.ErrorCode` enum으로 관리. 새 에러 추가 시 enum과 `GlobalExceptionHandler` 매핑을 함께 갱신.

## 4. 인증

- 헤더: **`X-Device-Id`**
- 어노테이션: `@DeviceId String deviceId`
- Resolver: `common/web/DeviceIdArgumentResolver`
- 미등록 기기: `ErrorCode.UNAUTHORIZED` → `401`
- 스펙 §0-3의 `Authorization: Bearer`는 향후 도입 — 지금은 X-Device-Id 유지

## 5. DTO / Entity

- DTO: Java `record` 선호 (불변)
- Controller에 `@Entity` 직접 노출 금지
- 변환은 Service 또는 DTO 정적 팩토리(`UserResponse.from(user)`)에서
- Repository는 가급적 Entity 또는 명시적 Projection만 반환

## 6. Swagger / 문서

- API 설명(`@Operation`, `@Schema(description = ...)`, `@Parameter`)은 **한국어**로 작성
- 헤더 파라미터(`X-Device-Id`)는 `@Parameter(in = ParameterIn.HEADER, required = true)`로 명시

## 7. 데이터베이스

- 로컬: MySQL 8 (`memoreel` DB, root/12345678) — `application-dev.yml`
- 운영: MySQL — 환경변수(`DB_URL`/`DB_USERNAME`/`DB_PASSWORD`) — `application-prod.yml`
- 테스트: H2 in-memory (`MODE=MySQL`) — `src/test/resources/application.yml`

### 스키마 변경

- `ddl-auto: update`는 컬럼·테이블 추가만 따라잡고, **인덱스/FK 변경은 반영하지 못한다**
- 인덱스나 컬럼 타입 변경 시 로컬 DB를 drop·재생성: `DROP DATABASE memoreel; CREATE DATABASE memoreel;`
- 변경 후 `./gradlew bootRun`으로 실제 MySQL 스키마에 반영됐는지 `DESCRIBE`, `SHOW INDEX`로 확인

## 8. 브랜치 / 커밋

브랜치: `junhyeon/{type}/#{이슈번호}/{간단한-설명}`

- 예: `junhyeon/chore/15/erd-apply`

커밋: `<type>: <한국어 제목>` (Conventional Commits)

- type: `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `style`
- 본문 1~3줄, "무엇을·왜"
- 푸터: `Closes #N` / `Refs #N`

## 9. 검증 명령

```bash
./gradlew installGitHooks   # 최초 1회
./gradlew spotlessApply     # 포맷 자동 수정
./gradlew harness           # 푸시 전 필수: spotless + ArchUnit + test
./gradlew bootRun           # dev 프로필로 로컬 실행
```

## 10. CI

- `.github/workflows/validate.yml` — PR + main push 시 `./gradlew harness` 자동 실행
- 실패 시 spotless/test 리포트가 artifact로 업로드됨
