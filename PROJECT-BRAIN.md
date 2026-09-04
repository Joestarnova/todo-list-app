# PROJECT-BRAIN

> Fullstack todo list app — Kotlin API + React client. Read this file first when returning.
> **Last updated:** 2026-09-02

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
- [x] **1.2 — Jackson + config** done (`e67cccf`): `jackson-datatype-jsr310` added, a shared `ObjectMapper` registers `KotlinModule` + `JavaTimeModule` and disables `WRITE_DATES_AS_TIMESTAMPS`, handed to Javalin via `config.jsonMapper(JavalinJackson(objectMapper))`. `Config.kt` reads `DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT` from the environment with local defaults, and the app now starts on `Config.port`.
- [x] Working tree is clean — everything through 1.2 is committed.

### What Is In Progress
- [ ] **1.3 — HikariCP datasource** (see Current Checkpoint below).

### What Is Broken / Incomplete
- No frontend yet; `client/` still holds only `.gitkeep`.
- Backend still only answers `GET /health`: no DB connection, no `/todos` routes, no Hikari pool.
- No PostgreSQL JDBC driver on the classpath yet — `Config.dbUrl` is just a string nothing uses.
- Nothing verifies the database is reachable, so the app currently starts happily with Docker stopped.

---

## Current Checkpoint

### Last Task Worked On
- **1.2 — Jackson + config** — done (2026-09-02). `build.gradle.kts` gained `jackson-datatype-jsr310:2.18.2` (version-matched to `jackson-module-kotlin`). `Main.kt` builds a top-level `objectMapper` with `registerKotlinModule()` + `registerModule(JavaTimeModule())` + `disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)` (so `java.time` values serialize as ISO strings, not epoch numbers) and passes it to Javalin as `JavalinJackson(objectMapper)`. `Config` is a Kotlin `object` (a singleton) using `System.getenv("X") ?: default`, with `PORT` parsed via `toIntOrNull()`. Committed as `e67cccf`.

### Current Progress
- [x] `jackson-datatype-jsr310` dependency added.
- [x] `JavalinJackson` registered as the JSON mapper with both modules.
- [x] `Config` object reading env vars.
- [x] App starts on `Config.port`.
- [ ] PostgreSQL JDBC driver dependency.
- [ ] HikariCP dependency.
- [ ] `Database.kt` exposing a `HikariDataSource`.
- [ ] Startup `SELECT 1` health check that fails fast.

### Next Task
**1.3 — HikariCP datasource**
- [ ] Add the PostgreSQL JDBC driver dependency (`org.postgresql:postgresql`) and HikariCP (`com.zaxxer:HikariCP`) to `server/build.gradle.kts`.
- [ ] New `server/src/main/kotlin/Database.kt` building a `HikariDataSource` from `Config` (`jdbcUrl` = `Config.dbUrl`, `username` = `Config.dbUser`, `password` = `Config.dbPassword`, `maximumPoolSize` ~5 for local).
- [ ] On startup, borrow a connection from the pool and run `SELECT 1`.
- [ ] Fail fast with a clear message if the DB is down — don't let Javalin start on a broken database.
- **Done when:** the app refuses to start with Docker stopped, and starts cleanly with it running.

Notes for this task:
- **What a connection pool is:** opening a Postgres connection is expensive (TCP + auth + session setup). A pool opens a few up front, keeps them alive, and lends one out per request. Think of it like a small pool of shared library cards rather than applying for a new one every time you want a book.
- **Why HikariCP:** it's the fast, boring default in the JVM world — the same pool Spring Boot ships with. It hands back a plain `javax.sql.DataSource`, so the rest of the code just calls `dataSource.getConnection()` and knows nothing about Hikari.
- **Why the JDBC driver is separate:** HikariCP only manages connections; it doesn't know how to speak Postgres's wire protocol. The `org.postgresql:postgresql` jar is what actually understands `jdbc:postgresql://...` URLs. Without it the pool fails with "No suitable driver".
- **Why `SELECT 1`:** the cheapest possible query. It proves the network, credentials, and database name are all correct. Hikari is lazy, so without this check a wrong password wouldn't surface until the first real request.
- **Fail fast** means: crash immediately at startup with a readable message instead of booting a server that will 500 on every route. In Kotlin this is a `try/catch` around the check that prints the cause and calls `exitProcess(1)` (or rethrows).
- Use `use { }` on the `Connection` and `Statement` — Kotlin's `use` closes the resource automatically when the block ends, returning the connection to the pool. Forgetting this leaks connections until the pool is exhausted.
- Pool size 5 is deliberate for local dev: more connections than a single dev machine needs is wasted memory on the Postgres side.
- Nothing in `Database.kt` should run SQL for todos yet — task 1.3 only proves the plumbing works.

### What's Blocking Me
- Nothing blocking.
- Docker must be running before testing the happy path (`open -a Docker`, then `docker info` to confirm). Testing the *sad* path is the point of this task — stop Docker (or `docker compose stop db`) and confirm the app refuses to boot.
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
| `server/build.gradle.kts` | Done through 1.2 — Kotlin JVM 2.3.0 + `application`, toolchain 25, Javalin 6.7.0, slf4j-simple, jackson-module-kotlin + jsr310. Task 1.3 adds the Postgres JDBC driver and HikariCP here. |
| `server/src/main/kotlin/Main.kt` | Done through 1.2 — Jackson mapper wired in, `GET /health` on `Config.port`. Task 1.3 adds the startup DB check here. |
| `server/src/main/kotlin/Config.kt` | Done — env-backed `dbUrl`, `dbUser`, `dbPassword`, `port` with local defaults. |
| `server/src/main/kotlin/Database.kt` | `TBD` (task 1.3) — builds the `HikariDataSource` from `Config` and runs the `SELECT 1` startup check. |
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
