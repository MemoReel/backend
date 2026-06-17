# MemoReel Backend - Project Guide

## Architecture (Domain + Layer)

Each domain package contains its own layer files. JPA `@Entity` classes are currently flattened under the shared `entity/` package. This is transitional; they can be moved to `{domain}/entity/` as domain boundaries become clearer.

```
com.memoreel.backend/
  {domain}/                  # e.g. user, song, keyword
    *Controller.java         # @RestController
    *Service.java            # @Service - business logic
    *Repository.java         # Spring Data JPA Repository
    dto/                     # request/response DTOs
  entity/                    # JPA entities (shared flattened package)
  common/                    # shared modules
    config/                  # e.g. JpaAuditingConfig
    error/                   # ErrorCode, BusinessException, GlobalExceptionHandler
    response/                # ApiResponse, PageResponse
    web/                     # @DeviceId, ArgumentResolver, WebConfig
    pagination/              # CursorCodec
```

### Dependency Rules (Enforced by ArchUnit)

- `*Controller` classes must not call `*Repository` classes directly; route access through services.
- Circular dependencies between packages are forbidden.
- Class placement: `*Controller` classes may live in `..controller..` or the domain root package. The same principle applies to `*Service` and `*Repository`.

Rule definitions: `src/test/java/com/memoreel/backend/architecture/ArchitectureRules.java`

## Authentication / Headers

- Current authentication method: **`X-Device-Id` header**
- Resolver: `common/web/DeviceIdArgumentResolver.java`, annotation: `@DeviceId String deviceId`
- Unregistered devices return `401 UNAUTHORIZED` (`ErrorCode.UNAUTHORIZED`).

## API Response Convention

- Success: `ApiResponse.success(data)` -> `{ "ok": true, "data": ... }`
- Failure: `GlobalExceptionHandler` converts exceptions automatically -> `{ "ok": false, "error": { code, message, details } }`
- Jackson: `SNAKE_CASE` + `default-property-inclusion: non_null` (`src/main/resources/application.yml`)
- Controllers return `ApiResponse<T>`. Metadata headers or status codes, such as `201 Created`, should be declared with `@ResponseStatus`.

## Coding Convention

- **Formatter**: Spotless + google-java-format (2-space indentation, import cleanup)
- Naming: `*Controller`, `*Service`, and `*Repository` suffixes are enforced by ArchUnit.
- DTOs: prefer Java `record` types for immutability.
- Do not expose entities directly from controllers. DTO conversion belongs in services or DTO static factories.
- Swagger API descriptions such as `@Operation` and `@Schema(description = ...)` should be written in Korean.
- Detailed conventions: `docs/specs/CONVENTIONS.md`

## Branch Convention

```
junhyeon/{type}/#{issue-number}/{short-description}
```

- Examples: `junhyeon/chore/15/erd-apply`, `junhyeon/feat/12/memo-crud`
- type: `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `style`

## Commit Message Convention

Conventional Commits (`<type>: <Korean subject>`).

- Body: 1-3 lines focused on what changed and why.
- Footer: `Closes #N` / `Refs #N`
- Example: `refactor: apply ERD - remove Photo entity and clean up records schema`

## Build and Verification

```bash
./gradlew installGitHooks   # install local git hooks (first run only)
./gradlew spotlessApply     # auto-format code
./gradlew spotlessCheck     # verify formatting
./gradlew test              # run tests, including ArchUnit
./gradlew harness           # full verification: spotless + ArchUnit + tests
./gradlew bootRun           # run locally with the dev profile
```

**Run `./gradlew harness` before every commit and push, and confirm it passes.**
- pre-commit: `spotlessCheck`
- pre-push: `harness`
- CI (`.github/workflows/validate.yml`): runs `harness` on PRs and pushes to `main`

## Slash Commands (`.claude/commands/`)

- `/harness-update {task}` - manage or update harness infrastructure such as Spotless, ArchUnit, hooks, and CI.

## Skills (`.claude/skills/`)

- `debug-and-verify-locally` - triggered automatically for bug reports such as "it does not work" or "there is an error"; verifies behavior directly with `bootRun` and curl.
