---
name: debug-and-verify-locally
description: Use this workflow when debugging bugs, error responses, or unexpected behavior in this repository. It must trigger when the user says something like "it does not work", "there is an error", "this is weird", "why is this happening", "debug this", or "please check this", or when the user reports that a specific endpoint does not work in local or production environments. Instead of guessing, start the server with ./gradlew bootRun, collect evidence with curl and logs, validate a hypothesis, then modify the code. After the fix, start the server again and verify the behavior. Apply this especially to areas where behavior cannot be inferred reliably from code alone, such as ArgumentResolver, authentication (X-Device-Id), GlobalExceptionHandler, and JPA mappings.
---

# Debug and Verify Locally

Follow this workflow when investigating bugs in this repository. The core rule is: do not guess -> run the system and collect evidence -> change code only after validating a hypothesis -> verify again after the fix.

## 1. When to Use This

- The user reports that a specific endpoint does not work, such as `/users`, `/me`, recommendation APIs, or saved-track APIs.
- The user provides an error response and asks for the cause or a fix, such as `status:500`, `401 UNAUTHORIZED`, `400 VALIDATION_ERROR`, or a JPA `ConstraintViolation`.
- Runtime behavior appears different from the code, especially in areas with indirect control flow such as `ArgumentResolver`, exception handlers, or JPA fetch/lazy behavior.
- The report is vague, such as "why is this happening" or "this is weird".

## 2. Workflow

If you skip this order, you will usually fix the wrong thing.

### Step 1. Restate the Symptom Precisely

Clarify what "cannot connect" or "does not work" means:

- Is it a 401, a 500, a strange response body, or a startup failure?
- Which input triggers it?
- Which environment is affected, dev or prod?

If a word can mean multiple things, ask the user one concise question. Before asking, inspect the code for 1-2 minutes to narrow the candidates. Do not ask empty-handed.

### Step 2. Read the Related Code and Git History First

- Read related files: controller, service, `GlobalExceptionHandler`, `application-*.yml`, `WebConfig`, and `DeviceIdArgumentResolver`.
- Run `git log --oneline -20`, then inspect diffs for recent commits that touched the affected area. Bugs usually live where the code changed most recently.
- Common culprits are newly added catch-all exception handlers, newly enabled authentication or filters, and changed routing.

### Step 3. Start the Server and Collect Evidence

Do not conclude from code alone. Run the server directly.

```bash
./gradlew bootRun
```

Run it in the background, but wait explicitly until one of these outcomes appears: `Started BackendApplication`, `APPLICATION FAILED TO START`, or `BUILD FAILED`.

If port 8080 is already in use, ask the user to stop the existing process. Do not kill a server that the user may have started.

The dev profile expects local MySQL (root/12345678, DB `memoreel`). If MySQL is not running, startup will fail, so check it first:

```bash
mysql -uroot -p12345678 -h127.0.0.1 -e "SHOW DATABASES LIKE 'memoreel';"
```

### Step 4. Send Requests with curl and Inspect Actual Responses

```bash
# Single request response + status code
curl -s -i http://localhost:8080/me -H "X-Device-Id: dev-001" | head -20

# JSON body
curl -s -X POST http://localhost:8080/users \
  -H "X-Device-Id: dev-001" \
  -H "Content-Type: application/json" \
  -d '{"nickname":"junhyeon"}' \
  -w "\nHTTP %{http_code}\n"
```

Compare the actual response line by line with the response format defined in `docs/`: snake_case and `ok`/`data`/`error`. Because `default-property-inclusion: non_null` is enabled, null fields are expected to be omitted.

### Step 5. Write One Hypothesis and Validate Only That Hypothesis

A good hypothesis looks like this: "X causes Y because Z."

- Do not edit code before validating the hypothesis.
- Validate one hypothesis at a time. If you change several things at once, you cannot know what worked.
- If the hypothesis is wrong, go back and form a new one. Do not keep trying random changes.

### Step 6. After the Fix, Restart and Verify Again

A passing build does not mean the behavior works.

- Modify the code.
- Stop the running server and restart it with `./gradlew bootRun`.
- Run the same curl scenario again and inspect the response.
- The pass criterion is: the result is the same as or better than the previous verification step.
- Check server logs as well: stack traces, `Caused by:`, and JPA `SQL warning` messages must not newly appear.

### Step 7. Report Verified Results

- Do not just say "fixed." Report the evidence, such as "`POST /users` -> 201, `data.user.id` is present, and the row exists in MySQL `users`."
- Inspect the response status and body directly. A catch-all handler can hide a 500 behind an incorrect 200.
- When asking the user to verify in their environment, state exactly what to check and how.

### Step 8. Separate Side Issues

If debugging reveals a separate problem, keep it outside the current PR scope and suggest tracking it separately.

## 3. Common Traps

| Trap | Why It Is Bad | Alternative |
|---|---|---|
| "No error log appeared, so it is fixed" | The response may still not be `200 OK`; a catch-all handler can hide failures. | Verify both the status code and body directly. |
| Switching between options A and B | Editing before validating hypotheses hides what actually fixed the issue. | Validate one hypothesis at a time and apply changes after decisive evidence. |
| Expecting `ddl-auto: update` to apply every schema change | Hibernate can add columns but does not reliably apply index or FK changes. | Run `DROP DATABASE memoreel; CREATE DATABASE memoreel;` and restart. |
| Tests pass but dev is broken | Tests use H2 with `profiles.active=test`; dev uses MySQL, so dialect behavior can differ. | Verify both with tests and by running dev locally. |
| Pre-commit/pre-push hooks do not run and CI fails after merge | `./gradlew installGitHooks` was not run. | Run it once after a fresh clone. |

## 4. Repository-Specific Notes

- Default profile: `dev` (`profiles.active: dev` in `application.yml`)
- Authentication: `X-Device-Id` header -> `DeviceIdArgumentResolver` -> `@DeviceId String deviceId`
- Unregistered devices return `ErrorCode.UNAUTHORIZED` (401).
- `GlobalExceptionHandler` converts `BusinessException` to `ApiResponse.error()` automatically.
- Responses use snake_case and `non_null`.
- Build verification: `./gradlew harness` (Spotless + ArchUnit + tests)

## 5. One-Line Summary

If you did not run it, you did not fix it.

