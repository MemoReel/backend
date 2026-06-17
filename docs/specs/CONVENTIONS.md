# CONVENTIONS

This document defines the project's coding, domain, and harness conventions. `CLAUDE.md` is the summary; this file is the detailed reference.

## 1. Package / Architecture

- Base package: `com.memoreel.backend`
- Domain directories such as `user/`, `song/`, and `keyword/` contain `*Controller`, `*Service`, `*Repository`, and `dto/`.
- Shared entities are currently flattened under `entity/`. As domains grow, they may be moved gradually to `{domain}/entity/`.
- Shared modules live under `common/`: `config/`, `error/`, `response/`, `web/`, and `pagination/`.

### Dependency Rules (Enforced by ArchUnit)

- `*Controller` -> `*Repository` direct calls are forbidden; route repository access through services.
- Circular dependencies between packages are forbidden (slices).
- Class placement: `*Controller`, `*Service`, and `*Repository` classes must live in `..controller..`, `..service..`, `..repository..`, or the domain root package.

Rule definitions: `src/test/java/com/memoreel/backend/architecture/ArchitectureRules.java`

## 2. Coding Style (Spotless + google-java-format)

- 2-space indentation, import cleanup, trailing whitespace removal, and final newline.
- Unused imports are removed automatically.
- Apply formatting with `./gradlew spotlessApply`; verify it with `spotlessCheck`.
- The pre-commit hook runs `spotlessCheck` automatically before every commit.

## 3. API Response

- Shared wrapper: `com.memoreel.backend.common.response.ApiResponse`
  - Success: `ApiResponse.success(data)` -> `{ "ok": true, "data": ... }`
  - Failure: `GlobalExceptionHandler` converts exceptions automatically -> `{ "ok": false, "error": { code, message, details } }`
- Jackson: `SNAKE_CASE` + `default-property-inclusion: non_null` (`application.yml`)
- Status codes: use `@ResponseStatus(HttpStatus.CREATED)` for creation and `204 No Content` for deletion.

### Error Codes

Manage error codes with the `com.memoreel.backend.common.error.ErrorCode` enum. When adding a new error, update both the enum and the `GlobalExceptionHandler` mapping.

## 4. Authentication

- Header: **`X-Device-Id`**
- Annotation: `@DeviceId String deviceId`
- Resolver: `common/web/DeviceIdArgumentResolver`
- Unregistered devices: `ErrorCode.UNAUTHORIZED` -> `401`
- The `Authorization: Bearer` flow from spec section 0-3 is planned for later; keep `X-Device-Id` for now.

## 5. DTO / Entity

- DTOs: prefer Java `record` types for immutability.
- Do not expose `@Entity` classes directly from controllers.
- Conversion belongs in services or DTO static factories such as `UserResponse.from(user)`.
- Repositories should return entities or explicit projections where possible.

## 6. Swagger / Documentation

- API descriptions such as `@Operation`, `@Schema(description = ...)`, and `@Parameter` should be written in Korean.
- Header parameters such as `X-Device-Id` must be declared with `@Parameter(in = ParameterIn.HEADER, required = true)`.

## 7. Database

- Local: MySQL 8 (`memoreel` DB, root/12345678) - `application-dev.yml`
- Production: MySQL via environment variables (`DB_URL` / `DB_USERNAME` / `DB_PASSWORD`) - `application-prod.yml`
- Tests: H2 in-memory (`MODE=MySQL`) - `src/test/resources/application.yml`

### Schema Changes

- `ddl-auto: update` only catches up with column and table additions; it does **not** reliably apply index or foreign key changes.
- For index or column type changes, drop and recreate the local DB: `DROP DATABASE memoreel; CREATE DATABASE memoreel;`
- After changing the schema, run `./gradlew bootRun` and verify the actual MySQL schema with `DESCRIBE` and `SHOW INDEX`.

## 8. Branch / Commit

Branch: `junhyeon/{type}/#{issue-number}/{short-description}`

- Example: `junhyeon/chore/15/erd-apply`

Commit: `<type>: <Korean subject>` (Conventional Commits)

- type: `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `style`
- Body: 1-3 lines focused on what changed and why.
- Footer: `Closes #N` / `Refs #N`

## 9. Verification Commands

```bash
./gradlew installGitHooks   # first run only
./gradlew spotlessApply     # auto-format code
./gradlew harness           # required before push: spotless + ArchUnit + tests
./gradlew bootRun           # run locally with the dev profile
```

## 10. CI

- `.github/workflows/validate.yml` runs `./gradlew harness` automatically on PRs and pushes to `main`.
- On failure, Spotless and test reports are uploaded as artifacts.
