# MemoReel Backend — Project Guide

## 아키텍처 (도메인 + 레이어)

도메인별 패키지 안에 레이어 디렉터리를 둔다. JPA `@Entity`는 현재 `entity/` 패키지로 평탄화돼 있다(과도기적 — 도메인 분리 시 `{도메인}/entity/`로 이동 가능).

```
com.memoreel.backend/
  {도메인}/                  # 예: user, song, keyword
    *Controller.java         # @RestController
    *Service.java            # @Service - 비즈니스 로직
    *Repository.java         # Spring Data JPA Repository
    dto/                     # 요청/응답 DTO
  entity/                    # JPA 엔티티 (공통 평탄화)
  common/                    # 공통 모듈
    config/                  # JpaAuditingConfig 등
    error/                   # ErrorCode, BusinessException, GlobalExceptionHandler
    response/                # ApiResponse, PageResponse
    web/                     # @DeviceId, ArgumentResolver, WebConfig
    pagination/              # CursorCodec
```

### 의존성 규칙 (ArchUnit으로 강제)

- `*Controller`는 `*Repository`를 **직접 호출하지 않는다** (Service 경유)
- 패키지 간 **순환 의존성 금지**
- 클래스 위치: `*Controller`는 `..controller..` 또는 도메인 루트 패키지, `*Service`/`*Repository`도 동일 원칙

규칙 정의: `src/test/java/com/memoreel/backend/architecture/ArchitectureRules.java`

## 인증 / 헤더

- 현재 인증 방식: **`X-Device-Id` 헤더**
- Resolver: `common/web/DeviceIdArgumentResolver.java`, 어노테이션: `@DeviceId String deviceId`
- 미등록 기기는 `401 UNAUTHORIZED` (`ErrorCode.UNAUTHORIZED`)

## API 응답 컨벤션

- 성공: `ApiResponse.success(data)` → `{ "ok": true, "data": ... }`
- 실패: `GlobalExceptionHandler`가 자동 변환 → `{ "ok": false, "error": { code, message, details } }`
- Jackson: `SNAKE_CASE` + `default-property-inclusion: non_null` (`src/main/resources/application.yml`)
- 컨트롤러는 `ApiResponse<T>`를 반환하되, 메타 헤더(예: 201 Created)는 `@ResponseStatus`로 명시

## 코딩 컨벤션

- **포맷터**: Spotless + google-java-format (2-space 인덴트, import 정리)
- 네이밍: `*Controller`, `*Service`, `*Repository` 접미사 (ArchUnit 강제)
- DTO: Java `record` 선호 (불변)
- Controller에 Entity 직접 노출 금지. DTO 변환은 Service 또는 DTO 정적 팩토리에서
- Swagger `@Operation` / `@Schema(description = ...)` 등 API 설명은 **한국어**
- 상세 컨벤션: `docs/specs/CONVENTIONS.md`

## 브랜치 컨벤션

```
junhyeon/{type}/#{이슈번호}/{간단한-설명}
```

- 예: `junhyeon/chore/15/erd-apply`, `junhyeon/feat/12/memo-crud`
- type: `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `style`

## 커밋 메시지 컨벤션

Conventional Commits (`<type>: <한국어 제목>`).

- 본문은 "무엇을·왜" 중심으로 1~3줄
- 푸터: `Closes #N` / `Refs #N`
- 예: `refactor: ERD 적용 — Photo 엔티티 제거 및 records 스키마 정리`

## 빌드 및 검증

```bash
./gradlew installGitHooks   # 로컬 git hook 설치 (초기 1회)
./gradlew spotlessApply     # 포맷팅 자동 수정
./gradlew spotlessCheck     # 포맷팅 검증
./gradlew test              # 테스트 (ArchUnit 포함)
./gradlew harness           # 전체 검증 (spotless + ArchUnit + test)
./gradlew bootRun           # 로컬 실행 (dev profile)
```

**모든 커밋·푸시 전에 `./gradlew harness`를 실행해 통과 확인.**
- pre-commit: `spotlessCheck`
- pre-push: `harness`
- CI(`.github/workflows/validate.yml`): PR/main push 시 `harness` 실행

## 슬래시 커맨드 (`.claude/commands/`)

- `/harness-update {작업}` — 하네스 인프라(Spotless, ArchUnit, 훅, CI) 관리/업데이트

## 스킬 (`.claude/skills/`)

- `debug-and-verify-locally` — "안 돼", "에러 떠" 등 버그 보고 시 자동 트리거. `bootRun` + curl로 직접 검증
