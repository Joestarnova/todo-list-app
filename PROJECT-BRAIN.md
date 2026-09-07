# PROJECT-BRAIN

> Fullstack todo list app — Kotlin API + React client. Read this file first when returning.
> **Last updated:** 2026-09-04

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
| JSON | Jackson + `jackson-module-kotlin` + `jackson-datatype-jsr310` |
| DB access | HikariCP 6.2.1 + PostgreSQL JDBC 42.7.4 (no ORM) |
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
- [x] **1.2 — Jackson + config** done (`e67cccf`): `jackson-datatype-jsr310` added, a shared `ObjectMapper` registers `KotlinModule` + `JavaTimeModule` and disables `WRITE_DATES_AS_TIMESTAMPS`, handed to Javalin via `config.jsonMapper(JavalinJackson(objectMapper))`. `Config.kt` reads `DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT` from the environment with local defaults, and the app now starts on `Config.port`.
- [x] **1.3 — HikariCP datasource** done (`565bae9`): `com.zaxxer:HikariCP:6.2.1` + `org.postgresql:postgresql:42.7.4` added. `Database.kt` exposes `createDataSource()` (jdbcUrl/username/password from `Config`, `maximumPoolSize = 5`) and `checkConnection()` running `SELECT 1` with nested `use { }` blocks. `Main.kt` wraps both in a `try/catch` that prints URL, user, cause and a "is Postgres running?" hint, then `exitProcess(1)` — so the app refuses to boot with the DB down.
- [x] Working tree is clean — everything through 1.3 is committed.

### What Is In Progress
- [ ] **1.4 — Model + read/create repository** (see Current Checkpoint below).

### What Is Broken / Incomplete
- No frontend yet; `client/` still holds only `.gitkeep`.
- Backend still only answers `GET /health`: the pool is alive but no SQL for todos is ever run, and there are no `/todos` routes.
- No Kotlin type mirrors the `todos` table yet — nothing to serialize.
- `dataSource` is created in `main()` but not yet passed to anything.

---

## Current Checkpoint

### Last Task Worked On
- **1.3 — HikariCP datasource** — done (2026-09-04, `565bae9`). Learned: a connection pool keeps a few Postgres connections open and lends them out; HikariCP hands back a plain `DataSource` so the rest of the code stays pool-agnostic; the JDBC driver is a *separate* jar because Hikari doesn't speak Postgres's wire protocol; `SELECT 1` at startup is the cheapest proof that network, credentials and db name are all correct, because Hikari is lazy and would otherwise hide a bad password until the first real request; `use { }` returns the connection to the pool automatically.

### Current Progress
- [x] PostgreSQL JDBC driver + HikariCP dependencies.
- [x] `Database.kt` exposing a `HikariDataSource`.
- [x] Startup `SELECT 1` health check that fails fast.
- [ ] `Todo` data class.
- [ ] `CreateTodoRequest` / `UpdateTodoRequest` DTOs.
- [ ] `TodoRepository` with `findAll()` and `create(text)`.
- [ ] Private `ResultSet.toTodo()` extension helper.
- [ ] Throwaway `main` that inserts a row and prints the list back.

### Next Task
**1.4 — Model + read/create repository**
- [ ] `Todo` data class with `id`, `text`, `done`, `createdAt` — matching the `todos` table (`BIGSERIAL` → `Long`, `TEXT` → `String`, `BOOLEAN` → `Boolean`, `TIMESTAMPTZ` → `java.time.OffsetDateTime`).
- [ ] Request DTOs: `CreateTodoRequest(text)` and `UpdateTodoRequest(...)` — what the *client* is allowed to send, which is not the same shape as what we send back.
- [ ] `TodoRepository` taking the `DataSource` (constructor parameter), with:
  - `findAll(): List<Todo>` — `SELECT * FROM todos ORDER BY created_at`.
  - `create(text: String): Todo` — `INSERT INTO todos (text) VALUES (?) RETURNING *`.
- [ ] A **private** `ResultSet.toTodo()` extension function that reads one row into a `Todo`. Reused by every query from here on.
- **Done when:** a throwaway `main` inserts a row and prints the list back.

Notes for this task:
- **Why a "model" and a "repository" are separate ideas:** the model (`Todo`) is just data — a Kotlin shape mirroring one database row. The repository is the *only* place that knows SQL. Everything above it (routes, later) asks the repository for todos and never touches JDBC. Think of the repository as the librarian: the rest of the app asks for a book, it alone knows the shelving system.
- **Why DTOs are separate from `Todo`:** DTO = Data Transfer Object, the shape crossing the wire. A client creating a todo sends only `{"text": "..."}` — it must not be able to set `id` or `createdAt`. If you reused `Todo` for input, Jackson would happily accept a client-supplied `id`. Separate types make the allowed input explicit.
- **Why `PreparedStatement` and not string concatenation:** `?` placeholders are sent to Postgres separately from the values, so a value can never be parsed as SQL. That's what kills SQL injection. Building SQL with `"... '$text'"` is the classic hole.
- **`RETURNING *`** is a Postgres extension: the `INSERT` hands back the row it just wrote, including the database-generated `id` and `created_at`. Without it you'd insert, then run a second `SELECT` to find out what you just made. Because it returns rows, run it with `executeQuery()`, not `executeUpdate()`.
- **What an extension function is:** `private fun ResultSet.toTodo(): Todo` adds a method onto `ResultSet` as if it were built in — inside it, `this` is the ResultSet, so `getLong("id")` reads the current row. It's `private` because it's an implementation detail of the repository. This avoids repeating the same four `get*` calls in every query method.
- **Column types:** read `created_at` with `getObject("created_at", OffsetDateTime::class.java)` — the plain `getTimestamp` loses the timezone that `TIMESTAMPTZ` carries.
- **Cursor mental model:** a `ResultSet` starts *before* the first row. `while (rs.next())` steps forward and returns false when rows run out — which is why `findAll` loops on it and a single-row `create` calls `rs.next()` once.
- Keep using `use { }` on the `Connection`, `PreparedStatement` and `ResultSet` — same reason as 1.3.
- The "throwaway `main`" is just a temporary `fun main()` (or a temporary block in the existing one) to prove the repository works before any HTTP route exists. Delete it in 1.5.

### What's Blocking Me
- Nothing blocking.
- Docker must be running before testing (`open -a Docker`, then `docker compose up -d` from the repo root) — the app now hard-fails at startup without it, by design.
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
| `schema.sql` | Done — the `todos` table (`id` BIGSERIAL, `text` TEXT, `done` BOOLEAN, `created_at` TIMESTAMPTZ). Edits require `docker compose down -v`. |
| `server/build.gradle.kts` | Done through 1.3 — Kotlin JVM 2.3.0 + `application`, toolchain 25, Javalin 6.7.0, slf4j-simple, Jackson (kotlin + jsr310), HikariCP, Postgres JDBC. Task 1.4 needs no new dependency. |
| `server/src/main/kotlin/Main.kt` | Done through 1.3 — fail-fast DB check, Jackson mapper wired in, `GET /health` on `Config.port`. Task 1.4 adds the throwaway insert/print here. |
| `server/src/main/kotlin/Config.kt` | Done — env-backed `dbUrl`, `dbUser`, `dbPassword`, `port` with local defaults. |
| `server/src/main/kotlin/Database.kt` | Done (1.3) — `createDataSource()` builds the `HikariDataSource`; `checkConnection()` runs `SELECT 1`. |
| `server/src/main/kotlin/Todo.kt` | `TBD` (task 1.4) — the `Todo` data class plus `CreateTodoRequest` / `UpdateTodoRequest`. |
| `server/src/main/kotlin/TodoRepository.kt` | `TBD` (task 1.4) — all SQL for todos: `findAll()`, `create(text)`, private `ResultSet.toTodo()`. |
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
