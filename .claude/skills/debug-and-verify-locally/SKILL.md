---
name: debug-and-verify-locally
description: 이 레포에서 발생한 버그, 에러 응답, 예상치 못한 동작을 디버깅할 때 사용한다. 사용자가 "안 돼", "에러 떠", "이상해", "왜 이래", "디버깅 해줘", "확인해줘" 같은 말을 하거나, 로컬·운영 환경에서 특정 엔드포인트가 동작하지 않는다고 보고할 때 반드시 트리거. 추측 대신 직접 ./gradlew bootRun 으로 서버를 띄우고 curl/로그로 증거를 수집해서 가설을 검증한 뒤 수정하고, 수정 후에도 다시 띄워서 동작을 확인하는 작업 방식을 강제한다. ArgumentResolver, 인증(X-Device-Id), GlobalExceptionHandler, JPA 매핑 같이 "코드만 봐서는 동작을 알기 어려운" 영역일수록 우선 적용.
---

# Debug and Verify Locally

이 레포의 버그를 추적할 때 따르는 작업 방식. 핵심은 **추측 금지 → 직접 실행해서 증거 수집 → 가설 검증 후에만 수정 → 수정 후 다시 검증**이다.

## 1. 언제 쓰는가

- 사용자가 특정 엔드포인트가 안 된다고 보고할 때 (`/users`, `/me`, 추천/저장 API 등)
- 에러 응답을 들고 와서 원인을 묻거나 고쳐달라고 할 때 (`status:500`, `401 UNAUTHORIZED`, `400 VALIDATION_ERROR`, JPA `ConstraintViolation` 등)
- 동작이 코드와 다르게 보이는 모든 경우 (ArgumentResolver, 예외 핸들러, JPA fetch/lazy 등 우회로가 많은 영역)
- "왜 이래" / "이상하다" 류 모호한 보고

## 2. 작업 순서

다음 순서를 깨면 거의 항상 잘못된 곳을 고치게 된다.

### Step 1. 증상을 사용자 말로 한 번 더 명확히 잡는다

"접속이 안 된다"는 말이 의미하는 것:
- 401? 500? 응답 body가 이상? 부팅 자체가 실패?
- 어떤 입력에서? 어떤 환경(dev/prod)에서?

추측이 갈리는 단어가 있으면 사용자에게 한 줄로 물어본다. 단, 사용자에게 묻기 전에 코드 1~2분 훑어서 후보를 좁힌 다음 묻는다. 빈손으로 묻지 않는다.

### Step 2. 관련 코드와 git 히스토리를 먼저 본다

- 관련 파일을 읽는다 (Controller, Service, `GlobalExceptionHandler`, `application-*.yml`, `WebConfig`, `DeviceIdArgumentResolver`)
- `git log --oneline -20` 그리고 해당 영역에 최근 손댄 커밋의 diff를 본다 — **버그는 보통 마지막에 손댄 곳에 있다**
- 최근에 추가된 catch-all 예외 핸들러, 새로 켠 인증/필터, 변경된 라우팅이 자주 범인

### Step 3. 직접 서버를 띄워 증거를 모은다

코드만 보고 결론 내지 않는다. 직접 띄운다.

```bash
./gradlew bootRun
```

백그라운드로 띄우되 부팅 완료를 명확히 기다린다 (`Started BackendApplication` / `APPLICATION FAILED TO START` / `BUILD FAILED` 셋 중 하나가 나올 때까지 polling).

포트 충돌(8080 already in use)이 나면 사용자에게 끄도록 요청한다. 사용자가 띄워둔 서버를 마음대로 죽이지 않는다.

dev 프로필은 로컬 MySQL(root/12345678, DB `memoreel`)을 기대한다. MySQL이 안 떠있으면 부팅이 실패하니 먼저 확인:

```bash
mysql -uroot -p12345678 -h127.0.0.1 -e "SHOW DATABASES LIKE 'memoreel';"
```

### Step 4. curl로 요청을 흘려보고 실제 응답을 본다

```bash
# 단일 요청 응답 + 상태코드
curl -s -i http://localhost:8080/me -H "X-Device-Id: dev-001" | head -20

# JSON 본문
curl -s -X POST http://localhost:8080/users \
  -H "X-Device-Id: dev-001" \
  -H "Content-Type: application/json" \
  -d '{"nickname":"junhyeon"}' \
  -w "\nHTTP %{http_code}\n"
```

스펙(`docs/`)이 정의한 응답 포맷(snake_case, `ok`/`data`/`error`)과 실제 응답을 줄별로 대조한다. `default-property-inclusion: non_null` 설정 때문에 null 필드는 응답에서 빠지는 게 정상.

### Step 5. 가설을 한 줄로 적고 그 가설만 검증한다

좋은 가설은 이렇게 생겼다: **"X가 Y를 일으킨다, 왜냐하면 Z이기 때문이다."**

- 가설 검증 전에는 코드 안 고친다
- 한 번에 한 가설씩 — 여러 곳을 동시에 고치면 무엇이 효과 있었는지 모른다
- 빗나가면 새 가설로 돌아간다. "한 번만 더 시도" 금지

### Step 6. 수정 후 반드시 다시 띄워서 검증한다

빌드가 통과한다는 것은 "동작한다"는 뜻이 아니다.

- 코드 수정
- 띄워둔 서버 종료 후 재기동 (`./gradlew bootRun`)
- 같은 curl 시나리오로 다시 응답 확인
- **"이전 검증 단계와 동일하거나 더 좋아졌는가"** 가 통과 기준
- 서버 로그도 같이: stack trace, `Caused by:`, JPA `SQL warning` 등이 새로 찍히지 않는지

### Step 7. 보고는 검증 결과로 한다

- "고쳤습니다"가 아니라 **"`POST /users` → 201 + `data.user.id` 정상, MySQL `users` row 확인"** 같이 검증한 사실을 말한다
- 응답 자체가 200/201/204 정상인지 직접 본다 — catch-all 핸들러가 500을 200으로 가리는 경우 있음
- 사용자 환경에서 한 번 더 확인 요청할 때는 무엇을 어떻게 확인해야 하는지 명시

### Step 8. 사이드 이슈는 분리한다

디버깅 중에 발견한 별개 문제는 **현재 PR 범위 밖**으로 빼고 별도 이슈로 안내한다.

## 3. 자주 빠지는 함정

| 함정 | 왜 안 좋은가 | 대안 |
|---|---|---|
| "에러 로그 안 떴으니 해결" | 200 OK 가 아닐 수도 있다 (catch-all 핸들러가 삼킴) | 응답 코드와 body까지 직접 확인 |
| 옵션 A·B 왔다갔다 | 가설 검증 없이 코드부터 만지면 어디서 풀렸는지 모름 | 한 가설씩 검증, 결정적 증거 후 적용 |
| `ddl-auto: update`로 스키마 변경 반영 안 됨 | Hibernate가 컬럼 추가는 해도 인덱스·FK 변경은 못 따라감 | `DROP DATABASE memoreel; CREATE DATABASE memoreel;` 후 재기동 |
| 테스트는 통과하는데 dev는 깨짐 | test는 H2 + `profiles.active=test`, dev는 MySQL — Dialect/방언 차이 | 둘 다 직접 띄워서 확인 |
| pre-commit/pre-push가 안 도는데 머지 후 CI에서 깨짐 | `./gradlew installGitHooks` 안 했음 | 새 클론 시 한 번 실행 |

## 4. 이 레포 특이사항

- 기본 프로필 `dev` (`application.yml`의 `profiles.active: dev`)
- 인증: `X-Device-Id` 헤더 → `DeviceIdArgumentResolver` → `@DeviceId String deviceId`
- 미등록 기기는 `ErrorCode.UNAUTHORIZED` (401)
- `GlobalExceptionHandler`가 `BusinessException` → `ApiResponse.error()` 자동 변환
- 응답은 snake_case + `non_null`
- 빌드 검증: `./gradlew harness` (spotless + ArchUnit + test)

## 5. 한 줄 요약

**띄우지 않고 고친 건 고친 게 아니다.**
