This skill manages and updates the harness engineering infrastructure.

## Managed Files

| File | Role |
|---|---|
| `CLAUDE.md` | AI agent guide |
| `docs/specs/CONVENTIONS.md` | Coding and domain conventions |
| `docs/specs/MAP.md` | Optional architecture map; add it if it does not exist yet |
| `docs/specs/ADR/*.md` | Optional architecture decision records |
| `src/test/java/com/memoreel/backend/architecture/*.java` | ArchUnit architecture tests |
| `build.gradle` (Spotless/ArchUnit dependencies and tasks) | Formatter, architecture, and harness task definitions |
| `.githooks/pre-commit`, `.githooks/pre-push` | Git hooks |
| `.github/workflows/harness.yml` | CI workflow |
| `CONTRIBUTING.md` | Guide for external contributors |

## Procedure

### Step 1: Understand the Current State

1. Analyze the user request (`$ARGUMENTS`) and decide which harness components need to change.
2. Read the current state of those components.

### Step 2: Apply Changes

Branch by request type:

#### Add an ADR
1. Check the highest existing number in `docs/specs/ADR/`; start from `001` if none exist.
2. Create a new ADR file with the next number.
3. Format: `{number}-{topic}.md` (for example, `003-jwt-auth.md`).
4. Update related harness files such as `CLAUDE.md` and `MAP.md`.

#### Add or Modify Spotless Rules (Java)
1. Modify the `spotless { java { ... } }` block in `build.gradle`.
2. If needed, update the `googleJavaFormat` version and options such as `removeUnusedImports` or `trimTrailingWhitespace`.
3. Reflect the convention in `docs/specs/CONVENTIONS.md`.
4. Run `./gradlew spotlessApply` to format existing code.

#### Add an ArchUnit Rule
1. Add a new rule method to `src/test/java/com/memoreel/backend/architecture/ArchitectureRules.java`.
2. Add a `@Test` that calls the rule in `LayerDependencyTest` or `NamingConventionTest`.
3. Create a new test class if needed.
4. Reflect dependency rules in `docs/specs/MAP.md` if that file exists.

#### Modify Conventions
1. Update `docs/specs/CONVENTIONS.md`.
2. Reflect the summary in `CLAUDE.md`.
3. If needed, update Spotless options or ArchUnit tests as well.

#### Modify Git Hooks / CI
1. Update `.githooks/*` or `.github/workflows/harness.yml`.
2. Synchronize local setup instructions in `CONTRIBUTING.md`.
3. When changing CI triggers such as branches or events, call that out explicitly in the PR notes.

### Step 3: Synchronize Documentation

When a change affects multiple files, keep related documents synchronized:
- `CLAUDE.md` <-> `docs/specs/CONVENTIONS.md`
- `docs/specs/MAP.md` if present <-> ArchUnit tests
- Commands and instructions in `CONTRIBUTING.md` <-> `build.gradle` tasks

### Step 4: Verify

```bash
./gradlew harness
```

If it fails, return to the change step. After it passes, summarize the changes for the user.

## Examples

- `/harness-update Add a new ADR: decide to introduce JWT authentication`
- `/harness-update Adjust the Spotless line-length option`
- `/harness-update Add an ArchUnit rule that common packages must not depend on domain packages`
- `/harness-update Add breaking-change notation to the commit message convention`
- `/harness-update Add a jar build step to CI`
