하네스 엔지니어링 인프라를 관리하고 업데이트하는 스킬입니다.

## 관리 대상 파일

| 파일 | 역할 |
|---|---|
| `CLAUDE.md` | AI 에이전트 가이드 |
| `docs/specs/CONVENTIONS.md` | 코딩/도메인 컨벤션 |
| `docs/specs/MAP.md` | (선택) 아키텍처 맵 — 아직 없으면 새로 추가 가능 |
| `docs/specs/ADR/*.md` | (선택) 아키텍처 결정 기록 |
| `src/test/java/com/memoreel/backend/architecture/*.java` | ArchUnit 아키텍처 테스트 |
| `build.gradle` (Spotless/ArchUnit 의존성·태스크) | 포맷터·아키텍처·하네스 태스크 정의 |
| `.githooks/pre-commit`, `.githooks/pre-push` | Git hooks |
| `.github/workflows/validate.yml` | CI 워크플로 |
| `CONTRIBUTING.md` | 외부 기여자용 가이드 |

## 절차

### 1단계: 현재 상태 파악

1. 사용자 요청(`$ARGUMENTS`)을 분석하여 어떤 하네스 구성요소를 수정해야 하는지 판단
2. 해당 구성요소의 현재 상태를 읽기

### 2단계: 수정 실행

요청 유형에 따라 분기:

#### ADR 추가
1. `docs/specs/ADR/` 디렉토리에서 마지막 번호 확인 (없으면 `001`부터 시작)
2. 다음 번호로 새 ADR 파일 생성 (한국어)
3. 형식: `{번호}-{주제}.md` (예: `003-jwt-auth.md`)
4. 관련된 다른 하네스 파일 업데이트 (CLAUDE.md, MAP.md 등)

#### Spotless 규칙 추가/수정 (Java)
1. `build.gradle`의 `spotless { java { ... } }` 블록 수정
2. 필요 시 `googleJavaFormat` 버전 갱신, `removeUnusedImports`/`trimTrailingWhitespace` 등 옵션 추가
3. `docs/specs/CONVENTIONS.md`에 관련 컨벤션 반영
4. `./gradlew spotlessApply`로 기존 코드 일괄 정렬

#### ArchUnit 규칙 추가
1. `src/test/java/com/memoreel/backend/architecture/ArchitectureRules.java`에 새 규칙 메서드 추가
2. 그 규칙을 호출하는 `@Test`를 `LayerDependencyTest` 또는 `NamingConventionTest`에 추가
3. 필요 시 새 테스트 클래스 생성
4. `docs/specs/MAP.md`(존재 시)에 의존성 규칙 반영

#### 컨벤션 수정
1. `docs/specs/CONVENTIONS.md` 수정
2. `CLAUDE.md`에 요약 반영
3. 필요 시 Spotless 옵션이나 ArchUnit 테스트도 함께 업데이트

#### Git Hook / CI 수정
1. `.githooks/*` 또는 `.github/workflows/validate.yml` 수정
2. `CONTRIBUTING.md`의 로컬 세팅 안내 동기화
3. CI 트리거(브랜치/이벤트) 변경 시 명시적으로 PR에 메모

### 3단계: 문서 동기화

수정 사항이 여러 파일에 영향을 미치는 경우, 관련 문서를 모두 동기화:
- `CLAUDE.md` ↔ `docs/specs/CONVENTIONS.md` 일관성
- `docs/specs/MAP.md`(존재 시) ↔ ArchUnit 테스트 일관성
- `CONTRIBUTING.md`의 명령/안내가 `build.gradle` 태스크와 일치

### 4단계: 검증

```bash
./gradlew harness
```

통과 못 하면 수정 단계로 돌아간다. 통과 후 변경 내용을 사용자에게 요약 보고.

## 사용 예시

- `/harness-update 새로운 ADR 추가: JWT 인증 도입 결정`
- `/harness-update Spotless 줄 길이 옵션 조정`
- `/harness-update ArchUnit에 common 패키지는 도메인 패키지를 의존하면 안 되는 규칙 추가`
- `/harness-update 커밋 메시지 컨벤션에 breaking change 표기법 추가`
- `/harness-update CI에 jar 빌드 단계 추가`
