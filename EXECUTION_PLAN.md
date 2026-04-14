# FinTrack — execution plan

Living document: update **Progress log** and checkboxes after each session so you can resume quickly.

| Source of truth | Use for |
|-----------------|--------|
| [README.md](./README.md) | Repo layout, build order, portfolio narrative |
| [BACKEND_REQUIREMENTS.md](./BACKEND_REQUIREMENTS.md) | Schema, Flyway, API §6–14, security, Lambdas, CI/CD |
| [FRONTEND_REQUIREMENTS.md](./FRONTEND_REQUIREMENTS.md) | Tailwind system, routes, pages §6, state, API layer, CI/CD |

---

## Resume here

| Field | Value |
|--------|--------|
| **Last updated** | 2026-04-14 (end of day) |
| **Repository state** | B2 complete — Flyway migrations V1–V8 created, PR #3 open awaiting merge |
| **Next action** | Merge PR #3, then start **B3** — Common layer (BACKEND §3 `common/`, §11) |
| **Blockers** | None |

**Start of next session:** merge PR #3, checkout main, pull, then implement `ApiResponse`, `PagedResponse`, `GlobalExceptionHandler`, domain exceptions, `RequestLoggingAspect` per BACKEND §11.

---

## Principles

1. **Backend before dependent UI** — auth and core entities should exist (or be stubbed consistently) before the matching Angular features.
2. **Verify each chunk** — after each numbered step, note what you ran (build, test, manual `curl`/browser).
3. **Map to docs** — requirement section numbers in parentheses for traceability.

---

## Branching strategy (GitHub Flow)

| Branch | Purpose | CI runs | Deploys to |
|--------|---------|---------|------------|
| `main` | Always deployable | Build + test + deploy | **Production** |
| `feature/*`, `fix/*`, `chore/*` | Work in progress | Build + test only | — |

**Workflow:**
1. Create branch from `main` (e.g. `feature/auth-module`)
2. Push commits → CI runs **build + test** (`.github/workflows/ci.yml`)
3. Open PR to `main` → review
4. Merge → deploy workflow runs (added in B13/C13)

---

## Phase A — Monorepo skeleton (optional first commit)

- [x] **A.0** Add top-level folders to match [README.md](./README.md) architecture: `fintrack-api/`, `fintrack-ui/`, `fintrack-lambdas/` (can hold `.gitkeep` until populated).
- [x] **A.1** Add root `.gitignore` suited to Java, Node, Angular, and IDE artifacts if not already present.

---

## Phase B — Backend (`fintrack-api/`)

Aligned with README “Build Order” and BACKEND §3–8, §11–14.

- [x] **B1** Project scaffold + `pom.xml` (Spring Boot 3.3, Java 21) + `FinTrackApplication.java` + `application.yml` / `application-dev.yml` / `application-prod.yml` (BACKEND §3, §14).
- [x] **B2** Flyway migrations **V1–V8** in `src/main/resources/db/migration/` (BACKEND §4–5).
- [ ] **B3** Common layer: `ApiResponse`, `PagedResponse`, `GlobalExceptionHandler`, domain exceptions, `RequestLoggingAspect` (BACKEND §3 `common/`, §11).
- [ ] **B4** Auth module: register, login, refresh, logout — JWT + refresh persistence, BCrypt, Spring Security filter chain (BACKEND §6.1, §7).
- [ ] **B5** User module: `/api/v1/users/me` profile, update name, password, soft-delete (BACKEND §6.2).
- [ ] **B6** Accounts CRUD + ownership + balance field rules (BACKEND §6.3).
- [ ] **B7** Categories list/create/update/delete with system vs custom rules (BACKEND §6.4).
- [ ] **B8** Transactions CRUD, filters, pagination, balance updates on write/delete, `GET /summary` (BACKEND §6.5).
- [ ] **B9** Budgets: monthly list with live utilisation, upsert, status thresholds (BACKEND §6.6).
- [ ] **B10** Reports: create job, status poll, download pre-signed URL; wire to Lambda/SQS/S3 as per BACKEND §6.7, §9.
- [ ] **B11** Logging: SLF4J/Logback JSON for prod (BACKEND §8).
- [ ] **B12** Tests: unit + integration for auth, account, transaction, budget (BACKEND §12).
- [ ] **B13** Dockerfile + Elastic Beanstalk config + GitHub Actions workflow (BACKEND §13; README workflows path).

---

## Phase C — Frontend (`fintrack-ui/`)

Aligned with README “Build Order” and FRONTEND §3–12, §14–15.

- [ ] **C1** Angular 19 scaffold + Tailwind v4 + `tailwind.config.ts` / `styles.css` tokens (FRONTEND §3–4).
- [ ] **C2** Core: HTTP providers, `auth.interceptor`, `error.interceptor`, `auth.guard`, `guest.guard`, `token-storage`, `api-response` model (FRONTEND §8–9).
- [ ] **C3** Shared UI: shell, sidebar, topbar, button/input/badge/spinner/modal/toast/empty-state; charts wrappers (FRONTEND §6.1, shared §3).
- [ ] **C4** Auth feature: login + register routes, forms, strength indicator, guest guard (FRONTEND §6.2, §5).
- [ ] **C5** Dashboard: stat cards, donut + bar charts, budget preview, recent transactions (FRONTEND §6.3).
- [ ] **C6** Transactions: list, filters, pagination, modal form (FRONTEND §6.5).
- [ ] **C7** Accounts: cards, modal form, delete confirm (FRONTEND §6.4).
- [ ] **C8** Budgets: month selector, cards, add/edit/delete (FRONTEND §6.6).
- [ ] **C9** Reports: generate modal, history table, polling, download (FRONTEND §6.7).
- [ ] **C10** Settings: profile + password sections + toasts (FRONTEND §6.8).
- [ ] **C11** Routing: lazy routes, `/` → dashboard, guards on features (FRONTEND §5, §12).
- [ ] **C12** Jest + Testing Library coverage for critical components/services (FRONTEND §13).
- [ ] **C13** `nginx.conf`, Dockerfile, GitHub Actions frontend deploy (FRONTEND §14–15).

---

## Phase D — AWS Lambdas (`fintrack-lambdas/`)

BACKEND §9 + README architecture.

- [ ] **D1** PDF generator Lambda (report job → PDF → S3 → status update).
- [ ] **D2** Budget alert Lambda (scheduler + SES) per spec.

---

## Phase E — Hardening & demo

- [ ] **E1** End-to-end smoke: register → account → transaction → budget → report request → download (dev/staged env).
- [ ] **E2** CORS, rate limits, and security headers verified against BACKEND §7.
- [ ] **E3** README quickstart: local run instructions for API + UI (update README if missing).

---

## Progress log

Append a row after each significant session (newest first).

| Date | Phase / IDs | Outcome | Commands / notes |
|------|-------------|---------|------------------|
| 2026-04-14 | B2 | Flyway migrations V1–V8 complete | All 8 migrations applied, PR #3 open |
| 2026-04-14 | A.0, A.1, B1 | Scaffold complete, CI workflow added | `mvnw -DskipTests package` ✓, `spring-boot:run` ✓, PR #1, PR #2 |
| 2026-04-14 | — | Plan file created; codebase not scaffolded yet | — |

---

## Daily check-in (copy when planning)

1. What did we finish since last time? → update checkboxes + Progress log.  
2. What is the single **next** step? → set **Next action** in “Resume here”.  
3. What proves it is done? → build, test, or explicit scenario.  
4. Any **blockers**? → document in “Resume here”.

When this file drifts from reality, fix **Resume here** first, then the checkboxes.
