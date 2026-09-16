# PROJECT-BRAIN

> Fullstack todo list app — Kotlin API + React client. Read this file first when returning.
> **Last updated:** 2026-09-16

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
- [x] **1.7 — Validation + error handling** done (`b24c760`): `ValidationException(message) : RuntimeException` in `Todo.kt`; `TodoRoutes.validateText()` trims, rejects blank, caps at `MAX_TEXT_LENGTH = 500`, and **returns the trimmed string** — POST and PUT both store the cleaned value. `Main.kt` registers `app.exception(ValidationException::class.java)` → 400 `{"error": ...}`, `app.exception(Exception::class.java)` → logs the stack trace via SLF4J and returns a generic 500, and `app.error(404)` → JSON instead of Javalin's HTML page. All five curl checks passed, including the trim case and `/health`.
- [x] **Update contract decided:** `PUT /todos/{id}` sends **both** `text` and `done`. `UpdateTodoRequest` fields are therefore non-nullable, so `jackson-module-kotlin` rejects a request missing either one — the type declaration *is* the validation.

### What Is In Progress
- [ ] **1.8 — CORS, logging, request testing** (see Current Checkpoint below).

### What Is Broken / Incomplete
- No frontend yet; `client/` still holds only `.gitkeep`.
- **No CORS headers.** A browser on `localhost:5173` will have its requests blocked before the frontend even exists. Task 1.8.
- **No request logging.** You cannot see the frontend's calls arriving. Task 1.8.
- **No repeatable API check.** Every verification so far has been hand-typed curl. Task 1.8 turns that into `api.http`.
- **No automated tests at all.** Task 1.9.
- **Thrown 404s lost their detail.** `app.error(404)` overwrites the body for *every* 404, so `NotFoundResponse("No todo with id 42")` now returns the generic `{"error": "404 not found"}`. Fix by adding `app.exception(NotFoundResponse::class.java)` — exception handlers write the body before error handlers run, so specific messages win while unmatched URLs still get the generic one.
- **`/api/todos/abc` returns 500, not 400.** `ctx.pathParam("id").toLong()` throws `NumberFormatException`, which the catch-all turns into "internal server error" — but it's bad input, not a server fault. Fix: `toLongOrNull() ?: throw ValidationException("id must be a number")` in both the PUT and DELETE handlers.
- Error message capitalization is inconsistent (`"Text must not be blank"` vs `"text must be at most ..."`). Cosmetic.

---

## Current Checkpoint

### Last Task Worked On
- **1.7 — Validation + error handling** — done and committed (2026-09-16, `b24c760`). Learned: there are **two** layers of safety net and they catch different things — `app.exception(X)` fires when an exception escapes a handler, `app.error(code)` fires when the *response status* ends up at `code`, including when no route matched and nothing was ever thrown. Javalin resolves exception handlers by walking the class hierarchy nearest-match-first, so a catch-all on `Exception` does not swallow the specific `ValidationException` handler. A custom exception was worth defining because Javalin's `BadRequestResponse` renders its own JSON shape and the API had promised `{"error": ...}`. And the security habit: `logger.error(msg, e)` puts the stack trace in *your* logs while the client gets a generic message — a leaked trace advertises your library versions and file paths.

### Current Progress
- [x] `ValidationException` + `validateText()` (trim → blank check → 500-char cap → return trimmed).
- [x] Validation wired into POST and PUT.
- [x] `app.exception(ValidationException)` → 400, `app.exception(Exception)` → logged generic 500.
- [x] `app.error(404)` → JSON not-found.
- [x] Verified with curl: blank → 400, 501 chars → 400, `/api/nope` → JSON 404, valid text → 201 and trimmed, `/health` → ok.
- [ ] `bundledPlugins.enableCors` for `http://localhost:5173`.
- [ ] Request logging.
- [ ] `api.http` with one request per endpoint.

### Next Task
**1.8 — CORS, logging, request testing**
- [ ] Enable CORS for `http://localhost:5173` (Vite's dev port) — Javalin 6 uses `config.bundledPlugins.enableCors { ... }`.
- [ ] Add request logging so the frontend's calls are visible as they hit the server.
- [ ] Save an `api.http` file (or Postman collection) with a request per endpoint — the regression check for the rest of the build.
- **Done when:** every request in `api.http` passes in one run.

Notes for this task:
- **What CORS actually is.** A browser rule, not a server rule. A page served from `localhost:5173` calling an API on `localhost:7070` is a *cross-origin* request (different port = different origin), and the browser blocks it unless the API replies with an `Access-Control-Allow-Origin` header naming that origin. Enabling CORS means "send that header." curl ignores all of this, which is why nothing has broken yet — the problem appears the moment a real browser is involved.
- **Preflight requests.** For anything beyond a simple GET — a PUT, a DELETE, a POST with `Content-Type: application/json` — the browser first sends an `OPTIONS` request asking permission, *then* the real one. The bundled plugin answers those automatically. If PUT/DELETE fail from the browser but work in curl, a failing preflight is the first suspect.
- **Name the origin, don't use `anyHost()`.** `anyHost()` is convenient and tempting; it tells every website on the internet that it may call your API. Fine for a throwaway demo, a bad habit to form.
- **Why logging matters here.** Once the frontend exists, "nothing happened" is ambiguous — did the request never leave the browser, or did it arrive and fail? A request log answers that instantly. Javalin's `config.requestLogger.http { ctx, ms -> ... }` gives you method, path, status, and duration.
- **What `api.http` is.** A plain text file of HTTP requests that VS Code (REST Client extension) or IntelliJ can execute inline — `###` separates requests. It replaces hand-typed curl with something re-runnable, and it lives in git so future-you inherits the checks. Use a variable (`@host = http://localhost:7070`) so one line changes every request later.
- **Order the file so it is self-contained.** POST a todo first, then use its id for PUT and DELETE, and finish with the `?done=true` cleanup — so one top-to-bottom run leaves the database as it found it. That is what makes "every request passes in one run" a meaningful check rather than a guess about leftover rows.

### Then
**1.9 — Backend tests**
- [ ] JUnit 5 + `javalin-testtools`; happy path per endpoint plus the blank-text 400.
- [ ] Point tests at a separate database (`todos_test`) **or** truncate the table in `@BeforeEach`.
- **Done when:** `./gradlew test` is green and the command is documented in the README.
- Note: this is where `api.http` stops being enough — it needs a running server and a human to read the output. Tests run themselves and fail loudly. Also note `Main.kt` currently builds the app inline; 1.9 will want a `createApp(repository): Javalin` function so a test can build one without `main()` starting a real server on port 7070.

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
| `.gitignore` | Covers OS, env, JVM/Gradle, IDE, Node, and `server/.kotlin/`. |
| `README.md` | Written; run commands still TODO. Task 1.9 must add `./gradlew test`. |
| `docker-compose.yml` | Done — `postgres:17` service `db`, volume `todo-pgdata`, port 5432, `schema.sql` init mount. |
| `schema.sql` | Done — the `todos` table (`id` BIGSERIAL, `text` TEXT, `done` BOOLEAN, `created_at` TIMESTAMPTZ). Edits require `docker compose down -v`. |
| `server/build.gradle.kts` | Done through 1.7 — Kotlin JVM 2.3.0 + `application`, toolchain 25, Javalin 6.7.0, slf4j-simple, Jackson (kotlin + jsr310), HikariCP, Postgres JDBC. Task 1.8 needs no new dependency (CORS is bundled); **task 1.9 adds `javalin-testtools` + JUnit 5**. |
| `server/src/main/kotlin/Main.kt` | Done — fail-fast DB check, Jackson mapper, two `app.exception` handlers, `app.error(404)`, `GET /health`, `TodoRoutes.register(...)`. Task 1.8 adds CORS + request logger to the `Javalin.create { }` config block. |
| `server/src/main/kotlin/Config.kt` | Done — env-backed `dbUrl`, `dbUser`, `dbPassword`, `port` with local defaults. |
| `server/src/main/kotlin/Database.kt` | Done — `createDataSource()` builds the `HikariDataSource`; `checkConnection()` runs `SELECT 1`. |
| `server/src/main/kotlin/Todo.kt` | Done — `Todo`, `CreateTodoRequest(text)`, `UpdateTodoRequest(text, done)` (both non-null: PUT contract), `ValidationException`. |
| `server/src/main/kotlin/TodoRepository.kt` | Done — all SQL lives here: `findAll`, `create`, `findById`, `update`, `delete`, `deleteCompleted`, private `ResultSet.toTodo()`. |
| `server/src/main/kotlin/TodoRoutes.kt` | Done (1.6-1.7) — `register(app, repository)` with the five REST endpoints plus the private `validateText()` / `MAX_TEXT_LENGTH`. Handlers stay thin. |
| `server/gradlew` | Done — the Gradle wrapper, committed. Nobody needs a system `gradle` install any more. |
| `server/api.http` | `TBD` (task 1.8) — one request per endpoint, runnable top-to-bottom. The regression check for the rest of the build. |
| `server/src/test/kotlin/` | `TBD` (task 1.9) — JUnit 5 + `javalin-testtools`. |
| `client/src/` | `TBD` — React entrypoint, TanStack Query client, API layer. |
| `client/vite.config.ts` | `TBD` — dev server + `/api` proxy to the backend. |

### API Surface (as of 1.7)
| Method | Path | Body | Success |
|---|---|---|---|
| GET | `/api/todos` | — | 200, `Todo[]` |
| POST | `/api/todos` | `{text}` | 201, `Todo` |
| PUT | `/api/todos/{id}` | `{text, done}` | 200, `Todo` / 404 |
| DELETE | `/api/todos/{id}` | — | 204 / 404 |
| DELETE | `/api/todos?done=true` | — | 200, count |

**Error contract (done, 1.7):** every failure responds with `{"error": "<readable message>"}` — `400` for validation failures, `404` for unknown ids *and* unknown routes, `500` generic for anything unexpected (real stack trace goes to the server log only). Text is trimmed and capped at 500 characters.

### Ports
| Service | Port |
|---|---|
| Postgres | 5432 |
| Kotlin API | 7070 |
| Vite dev server | 5173 (planned — the origin CORS must allow, task 1.8) |

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
