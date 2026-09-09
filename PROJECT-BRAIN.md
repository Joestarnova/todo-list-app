# PROJECT-BRAIN

> Fullstack todo list app — Kotlin API + React client. Read this file first when returning.
> **Last updated:** 2026-09-09

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
- [x] **1.4 — Model + read/create repository** done (`43c1771`): `Todo.kt` holds the `Todo` data class + `CreateTodoRequest` / `UpdateTodoRequest`. `TodoRepository.kt` holds `findAll()`, `create(text)`, and the private file-level `ResultSet.toTodo()` extension. Verified by a throwaway `main` — rows landed in Postgres with server-generated `id` and `created_at`.
- [x] **1.5 — Remaining repository methods** done (`f498f67`): `findById(id): Todo?`, `update(id, text, done): Todo?`, `delete(id): Boolean`, `deleteCompleted(): Int`. All six methods exercised manually — hit *and* miss paths both confirmed (`null`, `null`, `false`), DB verified empty afterwards.
- [x] **1.6 — REST routes** done (`b18d2c6`): `TodoRoutes.kt` holds `TodoRoutes.register(app, repository)` with all five endpoints — `GET /api/todos`, `POST /api/todos` (201), `PUT /api/todos/{id}`, `DELETE /api/todos/{id}` (204), `DELETE /api/todos?done=true`. Handlers are thin: parse -> one repository call -> status. Misses throw Javalin's `NotFoundResponse`; `?done=true` missing throws `BadRequestResponse`. The 1.5 throwaway block is gone from `Main.kt`, `.kotlin/` is ignored (`801e0c3`), and the `VAlUES` typo is fixed. Working tree clean.
- [x] **Update contract decided:** `PUT /todos/{id}` sends **both** `text` and `done`. `UpdateTodoRequest` fields are therefore non-nullable, so `jackson-module-kotlin` rejects a request missing either one — the type declaration *is* the validation.

### What Is In Progress
- [ ] **1.7 — Validation + error handling** (see Current Checkpoint below).

### What Is Broken / Incomplete
- No frontend yet; `client/` still holds only `.gitkeep`.
- **No input validation.** `POST {"text": " "}` currently inserts a blank todo. Task 1.7.
- **Unhandled exceptions leak.** A crash inside a handler returns Javalin's default 500, which can expose internals. Task 1.7.
- **Unknown URLs return HTML,** not JSON — breaks any JSON client. Task 1.7.
- **Error body shape is inconsistent.** Javalin's `NotFoundResponse` / `BadRequestResponse` render `{title, status, type, details}`, not the `{"error": "..."}` shape the API should speak everywhere.
- `ctx.pathParam("id").toLong()` throws `NumberFormatException` on `/api/todos/abc`, which becomes a 500 instead of a 400. Worth fixing alongside 1.7.

---

## Current Checkpoint

### Last Task Worked On
- **1.6 — REST routes** — done and committed (2026-09-09, `b18d2c6`). Learned: a route handler's job is translation, not decision-making — read the path param, deserialize the body, call one repository method, pick a status code. The repository's `Todo?` / `Boolean` returns pay off exactly here: `?: throw NotFoundResponse(...)` turns "no such row" into a real 404 without a separate existence query. Javalin catches its own `HttpResponseException` subclasses and converts them to real responses, so error paths never set a status by hand. And `/api/todos/{id}` vs `/api/todos?done=true` are two separate registrations — a query string is not part of the path.

### Current Progress
- [x] All five endpoints registered in `TodoRoutes.kt` and committed.
- [x] Throwaway 1.5 block removed from `Main.kt`; `.kotlin/` ignored; `VAlUES` typo fixed.
- [ ] `ValidationException` + `validateText()` helper.
- [ ] Wire validation into POST and PUT.
- [ ] `app.exception(...)` handlers in `Main.kt`.
- [ ] `app.error(404)` JSON handler.
- [ ] Verify with curl.

### Next Task
**1.7 — Validation + error handling**
- [ ] Reject blank text and text over ~500 chars with `400` and body `{"error": "text must not be blank"}`.
- [ ] `app.exception(Exception::class.java)` — log it, return a generic `500`. Never leak stack traces.
- [ ] `app.error(404)` — JSON not-found instead of Javalin's HTML page.
- **Done when:** posting `{"text": " "}` returns a 400 with a readable message.

Notes for this task:
- **Two layers of safety net, and why both are needed.** `app.exception(X::class.java)` fires when an exception of type `X` escapes a handler. `app.error(404)` fires whenever the *response status* ends up 404 — including when no route matched at all, which no exception handler can catch. Exception handlers catch throws; error handlers catch status codes.
- **Why a custom `ValidationException` instead of Javalin's `BadRequestResponse`:** `BadRequestResponse` renders Javalin's own JSON shape (`{title, status, type, details}`). Defining `class ValidationException(message: String) : RuntimeException(message)` and mapping it yourself gives the exact `{"error": "..."}` body the API promises.
- **`isBlank()` vs `isEmpty()`:** `isEmpty()` is only true for `""` — it lets `" "` straight through. `isBlank()` covers whitespace-only too. That single choice is the whole "Done when" condition.
- **Trim, then validate, then store the trimmed value.** `validateText()` should *return* the cleaned string so `"  buy milk  "` never reaches the database.
- **Handler order does not swallow HTTP responses.** Javalin checks for an exact-class user handler before falling back to its built-in `HttpResponseException` mapper, so registering `app.exception(Exception::class.java)` does **not** hijack `NotFoundResponse` / `BadRequestResponse`. They still produce their own status codes.
- **Log the real exception, return a generic message.** `logger.error("...", e)` on the server; `{"error": "internal server error"}` to the client. The stack trace is for you, not for whoever is poking at the API.
- **Validation belongs in the route layer here.** The repository reports facts (`null`, `false`, a row count); the routes decide HTTP policy. A blank-text rule is an input-contract rule, so it sits with the handler.

### What's Blocking Me
- Nothing blocking.
- Docker must be running before testing (`open -a Docker`, then `docker compose up -d` from the repo root) — the app hard-fails at startup without it, by design.
- Note for later schema edits: `/docker-entrypoint-initdb.d/` scripts run **only on first boot** of an empty volume. After changing `schema.sql`, run `docker compose down -v` to drop `todo-pgdata`, then `up -d` again.

---

## Important Context

### Important Files
| File | Why it matters |
|---|---|
| `PROJECT-BRAIN.md` | This file. Update it before ending each session. |
| `ui-design/todo-app.html` | The visual target for the React client. |
| `.gitignore` | Covers OS, env, JVM/Gradle, IDE, Node. **Needs `server/.kotlin/` added.** |
| `README.md` | Written; run commands still TODO until each half exists. |
| `docker-compose.yml` | Done — `postgres:17` service `db`, volume `todo-pgdata`, port 5432, `schema.sql` init mount. |
| `schema.sql` | Done — the `todos` table (`id` BIGSERIAL, `text` TEXT, `done` BOOLEAN, `created_at` TIMESTAMPTZ). Edits require `docker compose down -v`. |
| `server/build.gradle.kts` | Done through 1.6 — Kotlin JVM 2.3.0 + `application`, toolchain 25, Javalin 6.7.0, slf4j-simple, Jackson (kotlin + jsr310), HikariCP, Postgres JDBC. Task 1.7 needs no new dependency. |
| `server/src/main/kotlin/Main.kt` | Done — fail-fast DB check, Jackson mapper, `GET /health`, then `TodoRoutes.register(app, todoRepository)`. Task 1.7 adds the `app.exception(...)` and `app.error(404)` handlers here. |
| `server/src/main/kotlin/Config.kt` | Done — env-backed `dbUrl`, `dbUser`, `dbPassword`, `port` with local defaults. |
| `server/src/main/kotlin/Database.kt` | Done — `createDataSource()` builds the `HikariDataSource`; `checkConnection()` runs `SELECT 1`. |
| `server/src/main/kotlin/Todo.kt` | Done — `Todo`, `CreateTodoRequest(text)`, `UpdateTodoRequest(text, done)` (both non-null: PUT contract). Task 1.7 adds `ValidationException`. |
| `server/src/main/kotlin/TodoRepository.kt` | Done — all SQL lives here: `findAll`, `create`, `findById`, `update`, `delete`, `deleteCompleted`, private `ResultSet.toTodo()`. |
| `server/src/main/kotlin/TodoRoutes.kt` | Done (1.6) — `register(app, repository)` with the five REST endpoints, thin handlers only. Task 1.7 adds `validateText()` and calls it from POST and PUT. |
| `server/gradlew` | Done — the Gradle wrapper, committed. Nobody needs a system `gradle` install any more. |
| `client/src/` | `TBD` — React entrypoint, TanStack Query client, API layer. |
| `client/vite.config.ts` | `TBD` — dev server + `/api` proxy to the backend. |

### API Surface (as of 1.6; 1.7 adds the error contract)
| Method | Path | Body | Success |
|---|---|---|---|
| GET | `/api/todos` | — | 200, `Todo[]` |
| POST | `/api/todos` | `{text}` | 201, `Todo` |
| PUT | `/api/todos/{id}` | `{text, done}` | 200, `Todo` / 404 |
| DELETE | `/api/todos/{id}` | — | 204 / 404 |
| DELETE | `/api/todos?done=true` | — | 200, count |

**Error contract (task 1.7):** every failure responds with `{"error": "<readable message>"}` — `400` for validation failures, `404` for unknown ids *and* unknown routes, `500` generic for anything unexpected.

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
   Wipe it clean: `docker compose exec db psql -U todo -d todo -c 'TRUNCATE todos RESTART IDENTITY;'`
5. Start backend: `./gradlew run` from `/server`. Check: `curl localhost:7070/health` → `{"status":"ok"}`
6. Start frontend: `npm run dev` from `/client` — `TBD` until Vite is scaffolded.
7. **Before ending the session:** update *Current Progress*, *Next Task*, *What's Blocking Me*, and the *Last updated* date.
