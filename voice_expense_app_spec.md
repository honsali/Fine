# Voice Expense App — Unified Specification (EN)

> Personal micro‑project. Fully local. French UI texts inside the app but **spec is in English**. Currency is **MAD**. No backend. No tests. Agent auto‑commits.

---
## 0) Repository Policy (from AGENTS charter)
- This repo is fully agent‑driven. No human commits.
- After **every** change: agent commits & push to `main`.
- **No tests** (unit/instrumented/manualtest code) must live in repo.
- If any former spec asks for tests, this unified spec overrides it.

---
## 1) Navigation (Bottom Tabs)
Tabs:
1) **Ajouter** (Add by guided voice)
2) **Historique** (History list grouped by month)
3) **Gestion** (CSV export / purge / about)

Single‑activity using Navigation‑Compose. State preserved across tab switches.

---
## 2) Voice Flow — Functional (What must happen)
### Flow Navigation
```
[Initial] --tap Commencer--> [What(recording)]
[What] --Continuer--> [When(recording)]
[When] --Continuer--> [HowMuch(recording)]
[HowMuch] --Terminer--> [Validate&Save] --> [Initial]
[Any] --Annuler--> [Initial]
```

### Behaviour
- Initial: title "Enregistrer Une nouvelle Dépense", mic button "Commencer".
- Press → start FR‑speech + enter **What**.
- Live partial capture until user action.
- **Annuler** at any step stops recording, clears all fields, returns to Initial.
- **Répéter** clears only current step text and restarts recording for that step.
- **Continuer** moves What→When and When→HowMuch, auto‑restart recording each time.
- **Terminer** on HowMuch stops mic → parse (date & amount) → if fail: error & stay; if ok: persist + snackbar + reset Initial.

---
## 3) Parsing Rules (deterministic, local, no LLM)
### Amount (HowMuch)
- Accept: `120`, `120,5`, `120.50`, `1 200,75` + optional `dh|dhs|dirham(s)`.
- Normalize: strip currency → collapse spaces → single comma→dot → parse → *100 → store in **amount_minor INTEGER**.
- On failure: show error, block Terminer.

### Date (When)
- TZ = Africa/Casablanca. Display format = **dd/MM/yyyy**.
- Rules:
  - `aujourd’hui` → today
  - `hier` → today−1
  - weekday‑only ("jeudi") → **most recent ≤ today** (if today is Thu, pick today else last Thu)
  - day‑only ("le 24") → most recent 24 ≤ today else previous month’s 24 (wrap year)
  - absolute: `dd/MM/yyyy`, `dd/MM/yy`, `yyyy‑MM‑dd`
- Priority: Absolute → Aujourd’hui/Hier → Weekday → Day‑of‑month.
- Failure → error and block Terminer.

**NEVER use `SimpleDateFormat` (API26+). Use java.time (LocalDate, DateTimeFormatter).**

---
## 4) Data & Room
Entity `expenses`:
- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `description TEXT NOT NULL`
- `date TEXT NOT NULL` (ISO yyyy‑MM‑dd)
- `amount_minor INTEGER NOT NULL`
- `created_at INTEGER NOT NULL` (epoch millis)
- `source TEXT NOT NULL`

Indexes: on `date` and `created_at`.
Repository must persist on Terminer. No tests. No CI gates.

**amount_minor MUST BE INTEGER, never REAL.**

---
## 5) Historique Tab
- Group by (year, month) descending.
- Header per month: `<Month YYYY> — Total: X XXX,XX MAD`.
- Rows: 3 columns → Quoi (description), Quand (dd/MM/yyyy), Combien (formatted MAD from amount_minor).
- Empty state if zero rows.

---
## 6) Gestion Tab
- Export CSV (`date;description;amount_mad`) ordered by date ASC, share intent.
- Purge all with confirmation.
- About section.

---
## 7) Compose & Build Constraints
- Jetpack Compose single‑activity.
- Enable compose + BOM in Gradle.
- minSdk = 35 (spec and Gradle MUST match).
- No CI required. Local‑only.

---
## 8) Error & Permission Handling
- If mic permanently denied → CTA "Ouvrir les paramètres".
- Speech errors → snackbar; remain in same step; mic=false.
- Parser errors block Terminer but retain text to allow manual correction.

---
## 9) Acceptance
- Bottom tabs functional.
- Guided voice flow exactly matches state machine.
- Persistent DB insert only after valid parse.
- Month‑grouped history and totals rendered.
- CSV export works.
- No tests exist in repo.
- All constraints above enforced.

---
_End of unified spec._

