# Claude LLM 어댑터 설계 (음악 추천)

- 작성일: 2026-06-19
- 대상 도메인: `recommendation`
- 목적: `StubLlmAdapter` 대신 실제 Claude API를 호출하는 `ClaudeLlmAdapter`를 도입하여 사진 기반 음악 추천(Stage 1)과 재추천(Stage 2)을 운영 환경에서 동작시킨다.

## 1. 배경

현재 추천 파이프라인은 `LlmPort` 인터페이스로 추상화되어 있고, 유일한 구현체 `StubLlmAdapter`가 고정된 description/키워드/곡 후보를 반환한다. 운영 배포를 위해 실제 LLM 호출이 필요하다.

기존 인터페이스:
- `LlmAnalysis analyzePhoto(String photoUrl)` — Stage 1, 사진 분석
- `List<SongCandidate> recommendSongs(String description, List<String> keywordNames, List<SongCandidate> excludeTracks)` — Stage 2, 재추천(사진 재분석 없음)

이 시그니처는 그대로 유지하며, 새 어댑터만 추가한다.

## 2. 결정 사항 요약

| 항목 | 결정 |
|------|------|
| API 호출 방식 | 공식 Java SDK `com.anthropic:anthropic-java` |
| 모델 | `claude-sonnet-4-6` |
| 사진 전달 | `StoragePort.viewUrl(key)`로 발급한 S3 presigned URL을 image URL로 전달 |
| 응답 구조화 | tool use(function calling) 강제, `tool_choice = { type: "tool", name: ... }` |
| 키워드 선정 | DB `Keyword` 전체 목록을 시스템 프롬프트에 주입하고 그 목록에서만 선택하도록 강제 |
| 실패 폴백 | SDK 내장 재시도 이후에도 실패하면 5xx로 전파 (Stub 폴백 없음) |
| 프로파일 분기 | `memoreel.llm.provider` 프로퍼티로 stub/claude 토글, 기본값 stub |
| 키 관리 | `ANTHROPIC_API_KEY` 환경변수, yml에 평문 금지 |

## 3. 아키텍처

```
recommendation/
  adapter/
    StubLlmAdapter.java          (기존, @ConditionalOnProperty로 stub일 때 등록)
    ClaudeLlmAdapter.java        (신규, claude일 때 등록)
    claude/
      ClaudePromptBuilder.java   (시스템/유저 프롬프트 조립)
      ClaudeToolSchemas.java     (analyze_photo / recommend_songs tool 스키마 정의)
      ClaudeResponseParser.java  (tool_use 블록 → LlmAnalysis / List<SongCandidate>)
      ClaudeProperties.java      (@ConfigurationProperties)
  config/
    ClaudeClientConfig.java      (AnthropicClient 빈, claude 모드에서만)
```

`StubLlmAdapter`와 `ClaudeLlmAdapter`는 동시에 등록되지 않으므로 `@Primary`가 필요 없다. 기존 호출자(`LlmRecommendationAdapter`, `RecommendationService.retry`)는 수정하지 않는다.

### 의존성 추가

`build.gradle`에 `com.anthropic:anthropic-java` 추가. 정확한 버전은 구현 시 확정.

## 4. 데이터 흐름

### Stage 1 — `analyzePhoto(photoUrl)`

`photoUrl` 인자는 S3 key(예: `temp/uuid.jpg`)다.

1. `StoragePort.viewUrl(key)`로 presigned URL을 발급한다 (TTL 15분, 기존 구현 그대로).
2. `KeywordRepository.findAll()`로 키워드 목록을 가져온다. 어댑터가 `@PostConstruct` 시점에 한 번 캐싱하고, 필요 시 단순 갱신 메서드를 두지 않는다(키워드 추가 빈도가 낮다는 가정). 운영 중 새 키워드 반영이 필요하면 어플리케이션 재시작으로 처리한다.
3. Anthropic Messages API 호출:
   - `model`: `claude-sonnet-4-6`
   - `system`: 키워드 목록과 출력 규칙을 명시 (한국어 description, 키워드는 목록 내에서 1~3개, 곡 후보는 정확히 5개)
   - `user`: `[ImageBlock(source=url, url=presignedUrl), TextBlock("이 사진을 분석해 분위기에 어울리는 곡을 추천해줘")]`
   - `tools`: `[analyze_photo 스키마]`
   - `tool_choice`: `{ type: "tool", name: "analyze_photo" }`
4. 응답에서 `tool_use` 블록의 `input`(JSON)을 추출하여 `LlmAnalysis`로 매핑한다.

### Stage 2 — `recommendSongs(description, keywordNames, excludeTracks)`

사진 재분석 없음. 텍스트만.

1. Anthropic Messages API 호출:
   - `model`: `claude-sonnet-4-6`
   - `system`: "주어진 description/키워드/제외 곡을 받아 분위기에 맞는 새로운 곡 5개를 추천하라. 제외 목록의 곡은 포함하지 말 것."
   - `user`: description, keywordNames, excludeTracks(title+artist 목록)를 JSON 텍스트로 직렬화하여 전달
   - `tools`: `[recommend_songs 스키마]`
   - `tool_choice`: `{ type: "tool", name: "recommend_songs" }`
2. 응답에서 `tool_use.input` → `List<SongCandidate>` 매핑.

### Tool 스키마

`analyze_photo` input:
```json
{
  "type": "object",
  "required": ["description", "keyword_names", "candidates"],
  "properties": {
    "description": { "type": "string" },
    "keyword_names": {
      "type": "array",
      "items": { "type": "string" },
      "minItems": 1,
      "maxItems": 3
    },
    "candidates": {
      "type": "array",
      "minItems": 5,
      "maxItems": 5,
      "items": {
        "type": "object",
        "required": ["title", "artist"],
        "properties": {
          "title":  { "type": "string" },
          "artist": { "type": "string" }
        }
      }
    }
  }
}
```

`recommend_songs` input:
```json
{
  "type": "object",
  "required": ["candidates"],
  "properties": {
    "candidates": {
      "type": "array",
      "minItems": 5,
      "maxItems": 5,
      "items": {
        "type": "object",
        "required": ["title", "artist"],
        "properties": {
          "title":  { "type": "string" },
          "artist": { "type": "string" }
        }
      }
    }
  }
}
```

스키마로 강제해도 LLM이 위반할 수 있으므로 파서에서 한 번 더 검증한다 (5. 에러 처리 참고).

## 5. 에러 처리

### SDK 설정
- `maxRetries`: 2 (SDK 기본값. 429/5xx/네트워크는 자동 백오프 재시도)
- `timeout`: 30초 (`Duration.ofSeconds(30)`)
- 추가 수동 재시도는 두지 않는다.

### 예외 매핑

| 발생 상황 | 매핑 |
|----------|------|
| SDK가 재시도 후에도 5xx/타임아웃/네트워크 오류 throw | `BusinessException(UPSTREAM_ERROR, "LLM 호출 실패", null)` → 502 |
| SDK가 4xx(인증/요청 오류) throw | `BusinessException(UPSTREAM_ERROR, "LLM 호출 실패", null)` → 502 (운영자 알림 대상) |
| 응답에 `tool_use` 블록 없음 | `BusinessException(AI_ANALYSIS_FAILED, "LLM 응답 형식 오류", null)` → 422 |
| `tool_use.input` JSON 파싱 실패 | `BusinessException(AI_ANALYSIS_FAILED, "LLM 응답 형식 오류", null)` → 422 |
| `candidates.size() == 0` | `BusinessException(AI_ANALYSIS_FAILED, "LLM 응답에 곡 후보 없음", null)` → 422 |
| `candidates.size() < 5` | 그대로 사용 (이후 `SongResolver`가 iTunes 매칭으로 처리, 명세상 최대 5개 제한 존재) |
| `keyword_names`에 DB 미존재 값 포함 | 그대로 반환 (`KeywordRepository.findByNameIn`이 자연 필터링) |

`ErrorCode`는 기존 `UPSTREAM_ERROR`(502)와 `AI_ANALYSIS_FAILED`(422)를 사용한다. 신규 코드 추가 없음.

### 로깅
- 실패 시: `log.warn("Claude 호출 실패: stage={}, message={}", stage, e.getMessage())`
- 스택트레이스는 debug 레벨에서만.
- 응답 본문/입력 이미지 URL은 PII 위험 있으므로 debug 레벨에서만 노출.

## 6. 설정과 프로파일

### `application.yml` (기본값)
```yaml
memoreel:
  llm:
    provider: stub
    claude:
      api-key: ${ANTHROPIC_API_KEY:}
      model: claude-sonnet-4-6
      max-tokens: 1024
      timeout-seconds: 30
      max-retries: 2
```

### `application-prod.yml` (운영에서 override)
```yaml
memoreel:
  llm:
    provider: claude
```

### 빈 등록 조건
- `StubLlmAdapter`: `@ConditionalOnProperty(name = "memoreel.llm.provider", havingValue = "stub", matchIfMissing = true)`
- `ClaudeLlmAdapter`: `@ConditionalOnProperty(name = "memoreel.llm.provider", havingValue = "claude")`
- `ClaudeClientConfig`(= `AnthropicClient` 빈): `@ConditionalOnProperty(name = "memoreel.llm.provider", havingValue = "claude")`

`provider=stub`(default)에서는 `ANTHROPIC_API_KEY`가 없어도 부팅 가능하다.

### `ClaudeProperties` (record)
```java
@ConfigurationProperties("memoreel.llm.claude")
public record ClaudeProperties(
    String apiKey,
    String model,
    int maxTokens,
    int timeoutSeconds,
    int maxRetries) {}
```

`api-key`가 빈 문자열인 채로 `provider=claude`인 경우 부팅 실패시키도록 `@PostConstruct`에서 검증한다.

## 7. 테스팅

| 레벨 | 대상 | 방식 |
|------|------|------|
| Unit | `ClaudeLlmAdapter` | `AnthropicClient`를 Mockito로 mock. 정상/누락/잘못된 tool_use 응답을 주입해 매핑·예외 매핑 검증 |
| Unit | `ClaudeResponseParser` | 순수 함수 단위 테스트 (정상, 빈 candidates, 누락 필드 등) |
| Unit | `ClaudePromptBuilder` | 키워드 목록 주입, exclude 직렬화 등 |
| 기존 Integration | `RecommendationServiceTest` 등 | `provider=stub`(default) 그대로 동작, 변경 없음 |
| Manual | 실제 Claude 호출 검증 | `ANTHROPIC_API_KEY` 설정 + `--memoreel.llm.provider=claude` 플래그로 `./gradlew bootRun` |

`ArchUnit`: `ClaudeLlmAdapter`는 `recommendation.adapter` 패키지에 위치하므로 기존 규칙에 위반되지 않는다. 새 규칙 불필요.

`Spotless`: 기존 google-java-format 규칙을 그대로 통과해야 한다.

## 8. 비용/보안 메모

- `ANTHROPIC_API_KEY`는 환경변수에서만 주입. yml/소스코드에 평문 금지.
- 운영 로그에 응답 본문/이미지 URL을 직접 노출하지 않는다.
- 서킷브레이커, 레이트 리밋, 비용 모니터링은 본 작업 범위 밖. 트래픽 데이터 확보 후 별도 작업으로 결정.
- presigned URL TTL이 15분이므로 Claude 호출은 발급 직후 한 번에 완료된다(재시도 합산 시간도 30초 미만으로 충분).

## 9. 변경되는/추가되는 파일

추가:
- `recommendation/adapter/ClaudeLlmAdapter.java`
- `recommendation/adapter/claude/ClaudePromptBuilder.java`
- `recommendation/adapter/claude/ClaudeToolSchemas.java`
- `recommendation/adapter/claude/ClaudeResponseParser.java`
- `recommendation/adapter/claude/ClaudeProperties.java`
- `recommendation/config/ClaudeClientConfig.java`
- `application-prod.yml` (없는 경우 신규)
- 테스트: `ClaudeLlmAdapterTest`, `ClaudeResponseParserTest`, `ClaudePromptBuilderTest`

수정:
- `build.gradle` — `com.anthropic:anthropic-java` 의존성 추가
- `application.yml` — `memoreel.llm.*` 설정 블록 추가
- `StubLlmAdapter.java` — `@Component`에 `@ConditionalOnProperty(... havingValue="stub", matchIfMissing=true)` 추가

변경 없음:
- `LlmPort`, `LlmAnalysis`, `SongCandidate`
- `LlmRecommendationAdapter`, `RecommendationService`
- `StoragePort`, `S3StorageAdapter`
- `ErrorCode`(기존 코드 재사용)

## 10. 비범위 (Out of Scope)

- 새 키워드 추가에 대한 동적 갱신(현재는 재시작으로 처리).
- 비용/사용량 모니터링, 서킷브레이커, 레이트 리밋.
- 프롬프트 A/B 테스트 인프라.
- 사진 외 다른 입력(텍스트만으로 추천 시작 등) 지원.
