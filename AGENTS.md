# AGENTS.md — Project Automation Charter (Personal Vibe-Coding)

> This is a tiny **personal** project. No sensitive code. I (the user) do **not** want to touch any code, build, commit, or run tests. The agent (Codex GPT-5 or equivalent) does **everything** automatically without asking me.

## 1) Intent
- Ship features fast for my personal use.
- No manual developer actions from me.
- Keep the repo always up-to-date on `main`.

## 2) Autonomy & Permissions
- The agent has **full autonomy** to edit, create, rename, move, and delete files in this repository **without approval**.
- The agent must **commit and push after every change** (one small commit per atomic change is preferred; batching is allowed only when strictly necessary to keep the build consistent).
- The agent does **not** require my confirmation for any change.

## 3) Scope of Work
- Implement the **Ajouter / Historique / Gestion** tabs, local Room database, and the voice-led flow (Quoi → Quand → Combien) exactly as specified in `Voice Expense App Spec V2`.
- Implement the **deterministic parser** for dates (`aujourd’hui`, `hier`, weekday-only → most recent, day-of-month-only → most recent), and amounts in MAD.
- Keep everything local/offline. No backend. No LLM calls for parsing.
- Maintain and update documentation files (e.g., `README.md`, this `AGENTS.md`).

## 4) Testing Policy (Simplified)
- **No tests required.**
- Remove **all test code** and test directories from the project tree (`src/test`, `src/androidTest`, any `test/` folders). Do not add new tests.
- Remove test-related plugins and tasks from build scripts if present.

## 5) CI/CD
- **No CI pipelines** are needed for this project. Remove existing workflows that run tests or lint gates if they block pushes.
- Optional: keep a minimal workflow that just builds the app if and only if needed for release artifacts; otherwise disable all workflows.

## 6) Git & Branching Rules
- Single branch: **`main`**.
- After **each code modification**, the agent must:
  1. Stage the change(s)
  2. Commit with a short, descriptive message
  3. Push to `origin/main`
- Commit message style: keep it simple. Examples:
  - `feat(add): initial Quoi/Quand/Combien flow`
  - `fix(parser): handle "le 24" to most recent date`
  - `chore(room): create Expense entity`
- Force-push is allowed **only** to fix an obviously broken commit pushed seconds/minutes earlier. Prefer normal push.

## 7) Build & Artifacts
- Building locally is **not required** for me. The agent may build locally as needed to verify changes, but it should not depend on me to run anything.
- If release APKs/AABs are produced in the future, the agent may place them under a `releases/` folder or provide GitHub Releases — up to the agent.

## 8) Security & Secrets
- No secrets are expected in this project. Do **not** add tokens or credentials to the repository.
- If a token is needed for push (e.g., CI context), use the configured environment (never commit secrets).

## 9) UX/Content Rules (Summary)
- French-only labels and messages.
- Currency: **MAD** (dirham). Internally store amounts as **centimes** (minor units).
- Date display format: **dd/MM/yyyy** everywhere in the UI History table.
- History grouped by month with monthly totals in header.

## 10) Guardrails for the Agent
- Do **not** add tests back.
- Do **not** introduce mandatory CI checks that would block pushes.
- Keep commits small and frequent.
- Keep the project compiling; if a change breaks the build, immediately follow up with a fix commit and push.
- Keep documentation minimal but accurate (`README.md`, `AGENTS.md`).

## 11) Repository Cleanup (one-time)
- Delete any `test` or `androidTest` directories and test sources.
- Remove test dependencies from Gradle files (JUnit, Espresso, Robolectric, etc.).
- Remove CI workflows that run tests or lint gates.
- Update `README.md` to reflect the personal, auto-commit, no-test policy.

---

### TL;DR for the Agent
- You have full write access. Make changes autonomously.
- **Commit & push after every change**. No approvals, no questions.
- **No tests**. Remove them and do not add new ones.
- Keep `main` always green and usable for me.

## 12) Repository Context (Paths & Layout)
- **App code**: `app/src/main/java/...`
- **Resources**: `app/src/main/res/...`
- **Gradle**: `app/build.gradle.kts`, `settings.gradle.kts`, `build.gradle.kts`
- **Docs**: `/voice_expense_app_spec.md`, `/AGENTS.md`
- **Releases (optional)**: `/releases/`

> The **spec** under `/docs/voice_expense_app_spec.md` MUST align with this charter:
> - **No tests** required in this repository (unit/instrumented). Any previous mentions of tests in the spec are superseded by AGENTS.md.
> - If validation is mentioned (e.g., CSV/export), it refers to **manual checks** or simple runtime sanity, not automated tests.
