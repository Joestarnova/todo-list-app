# PROJECT-BRAIN

> Fullstack todo list app — Kotlin API + React client. Read this file first when returning.
> **Last updated:** 2026-09-01

---

## Project Overview

### What This Project Is
- A todo list web app: create, complete, and delete tasks.
- Split repo: `/server` (Kotlin + Javalin REST API), `/client` (React + TypeScript SPA).
- UI already designed as a static mockup — dark theme, single centered checkbox list.
- Personal project. Goal: a working local fullstack app runnable via Docker Compose.

### Tech Stack
**Frontend** (`/client`)
| Purpose | Technology |
|---|---|
| UI library | React 19 |
| Language | TypeScript |
| Build tool | Vite |
| Styling | Tailwind CSS v4 |
| Server state | TanStack Query |

**Backend** (`/server`)
| Purpose | Technology |
|---|---|
| Language | Kotlin 2.x |
| Runtime | JDK 25 (Temurin 25.0.4.1, `JAVA_HOME` set) |
| Web framework | Javalin 6 |
| Logging | `slf4j-simple` (required — without it Javalin logs nothing) |
| Build tool | Gradle (Kotlin DSL) |
| JSON | Jackson + `jackson-module-kotlin` |
| DB access | HikariCP + JDBC (no ORM) |
| Database | PostgreSQL 17 (via Docker Compose) |
| Schema | `schema.sql` (hand-written, no migrations tool) |

**Repo:** `github.com/Joestarnova/todo-list-app` (`origin` already set)

---

## Current State

### What Currently Works
- [x] Git repo initialized, `origin` remote configured.
- [x] UI mockup exists: `ui-design/todo-app.html`.
- [x] **0.1 — Repo skeleton** done: folder layout, expanded `.gitignore`, root `README.md`.
- [x] **0.2 — Postgres + schema** done: `docker-compose.yml` runs `postgres:17` as service `db`, `schema.sql` mounted into `/docker-entrypoint-initdb.d/`, `todos` table verified in the running container.
- [x] DB connection details: host `localhost:5432`, db `todo`, user `todo`, password `todo`.
- [x] **1.1 — Gradle + Javalin hello world** done: Gradle wrapper committed in `/server`, `build.gradle.kts` (Kotlin JVM 2.3.0 + `application`, toolchain 25, Javalin 6.7.0, slf4j-simple 2.0.17), `Main.kt` serving `GET /health` on port 7070.
- [x] Working tree is clean — everything through 1.1 is committed (`a0219c7`, then `cbf08fd` ignoring `CLAUDE.md`).

### What Is In Progress
- [ ] **1.2 — Jackson + config** (see Current Checkpoint below).

### What Is Broken / Incomplete
- No frontend yet; `client/` still holds only `.gitkeep`.
- Backend is a hello-world only: no DB connection, no `/todos` routes, no Hikari pool.
- `jackson-module-kotlin` 2.18.2 is declared in `build.gradle.kts` but **not yet wired into Javalin** — Javalin still uses its own default Jackson mapper, which has no Kotlin support.
- No `Config` object — port 7070 and the DB credentials are not read from the environment anywhere.
- `jackson-datatype-jsr310` (the `JavaTimeModule`) is **not** a dependency yet; needed before `created_at` can serialize.

---

## Current Checkpoint

### Last Task Worked On
- **1.1 — Gradle + Javalin hello world** — done (2026-09-01). The Gradle wrapper got scaffolded into `/server` (this cleared the old blocker), `build.gradle.kts` written with Kotlin JVM + `application`, `jvmToolchain(25)`, Javalin 6.7.0 and slf4j-simple. `Main.kt` starts Javalin on 7070 and answers `GET /health` with `{"status":"ok"}`. Committed as `a0219c7`; `jackson-module-kotlin` was added to the dependency block at the same time but is not registered with Javalin yet.

### Current Progress
- [x] Gradle wrapper exists and is committed in `/server`.
- [x] `build.gradle.kts` written.
- [x] `Main.kt` written — `GET /health` on port 7070.
- [x] `./gradlew run` boots and `/health` responds.
- [x] `jackson-module-kotlin` dependency declared.
- [ ] `jackson-datatype-jsr310` dependency added.
- [ ] `JavalinJackson` registered as the JSON mapper with both modules.
- [ ] `Config` object reading env vars.
- [ ] Data-class serialization sanity check.

### Next Task
**1.2 — Jackson + config**
- [ ] Add `jackson-datatype-jsr310` alongside the existing `jackson-module-kotlin` (needed for `JavaTimeModule` / `created_at`).
- [ ] Build an `ObjectMapper` that registers `KotlinModule` **and** `JavaTimeModule`, and hand it to Javalin as `JavalinJackson(mapper)` via `Javalin.create { it.jsonMapper(...) }`.
- [ ] Small `Config` object reading `DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT` from the environment with local defaults (`jdbc:postgresql://localhost:5432/todo`, `todo`, `todo`, `7070`).
- [ ] Have `Main.kt` start on `Config.port` instead of the hard-coded 7070.
- [ ] Temp route returning a Kotlin data class (one with an `Instant`/`LocalDateTime` field) to confirm camelCase JSON.
- **Done when:** a data class round-trips to JSON without a "no serializer" error.

Notes for this task:
- **Why this is needed at all:** Javalin bundles plain Jackson, which is a Java library — it can't see Kotlin constructor parameter names or handle non-nullable/default values. Without `KotlinModule` a data class either fails outright or deserializes with nulls in non-null fields.
- **Why `JavaTimeModule` is separate:** plain Jackson doesn't know `java.time` types either; `created_at` would serialize as a nested object of fields instead of an ISO timestamp string.
- Keep the version of `jackson-datatype-jsr310` matched to `jackson-module-kotlin` (2.18.2) to avoid mixing Jackson versions.
- Env vars are read with `System.getenv("NAME") ?: "default"` — the `?:` (elvis) operator supplies the local fallback so the app still runs with nothing exported.
- Nothing here connects to Postgres yet; `Config` just *holds* the DB values for the next task.
- The temp sanity-check route is scaffolding — delete it once the real `/todos` routes exist.

### What's Blocking Me
- Nothing blocking. The Gradle-wrapper blocker from 0.2/1.1 is resolved.
- JDK 25 is installed and `JAVA_HOME` points at Temurin 25 — this check is cleared.
- Docker must be running before any DB work (`open -a Docker`, then `docker info` to confirm).
- Note for later schema edits: `/docker-entrypoint-initdb.d/` scripts run **only on first boot** of an empty volume. After changing `schema.sql`, run `docker compose down -v` to drop `todo-pgdata`, then `up -d` again.

---

## Important Context

### Important Files
| File | Why it matters |
|---|---|
| `PROJECT-BRAIN.md` | This file. Update it before ending each session. |
| `ui-design/todo-app.html` | The visual target for the React client. |
| `.gitignore` | Done — covers OS, env, JVM/Gradle, IDE, Node. |
| `README.md` | Written; run commands still TODO until each half exists. |
| `docker-compose.yml` | Done — `postgres:17` service `db`, volume `todo-pgdata`, port 5432, `schema.sql` init mount. |
| `schema.sql` | Done — the `todos` table (`id`, `text`, `done`, `created_at`). Edits require `docker compose down -v`. |
| `server/build.gradle.kts` | Done — Kotlin JVM 2.3.0 + `application`, toolchain 25, Javalin 6.7.0, slf4j-simple, jackson-module-kotlin. Task 1.2 adds `jackson-datatype-jsr310` here. |
| `server/src/main/kotlin/Main.kt` | Done (hello world) — Javalin on 7070, `GET /health`. Task 1.2 edits it to register `JavalinJackson` and use `Config.port`. |
| `server/src/main/kotlin/Config.kt` | `TBD` (task 1.2) — env-backed `DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT` with local defaults. |
| `server/gradlew` | Done — the Gradle wrapper, committed. Nobody needs a system `gradle` install any more. |
| `client/src/` | `TBD` — React entrypoint, TanStack Query client, API layer. |
| `client/vite.config.ts` | `TBD` — dev server + `/api` proxy to the backend. |

### Ports
| Service | Port |
|---|---|
| Postgres | 5432 |
| Kotlin API | 7070 |
| Vite dev server | 5173 (planned) |

---

## Quick Resume Guide

1. Read this file top to bottom (~1 min).
2. `git log --oneline -5` and `git status` — confirm reality matches the checkpoint above.
3. Jump to **Current Checkpoint → Next Task** and start on the first unchecked box.
4. Start Docker Desktop (`open -a Docker`), then the database from the repo root: `docker compose up -d`.
   Check the table: `docker compose exec db psql -U todo -d todo -c '\d todos'`
5. Start backend: `./gradlew run` from `/server`. Check: `curl localhost:7070/health` → `{"status":"ok"}`
6. Start frontend: `npm run dev` from `/client` — `TBD` until Vite is scaffolded.
7. **Before ending the session:** update *Current Progress*, *Next Task*, *What's Blocking Me*, and the *Last updated* date.
