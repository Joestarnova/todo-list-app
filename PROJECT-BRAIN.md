# PROJECT-BRAIN

> Fullstack todo list app — Kotlin API + React client. Read this file first when returning.
> **Last updated:** 2026-09-08

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
- [x] **1.5 — Remaining repository methods** done (uncommitted): `findById(id): Todo?`, `update(id, text, done): Todo?`, `delete(id): Boolean`, `deleteCompleted(): Int`. All six methods exercised manually — hit *and* miss paths both confirmed (`null`, `null`, `false`), DB verified empty afterwards.
- [x] **Update contract decided:** `PUT /todos/{id}` sends **both** `text` and `done`. `UpdateTodoRequest` fields are therefore non-nullable, so `jackson-module-kotlin` rejects a request missing either one — the type declaration *is* the validation.

### What Is In Progress
- [ ] **1.6 — REST routes** (see Current Checkpoint below).

### What Is Broken / Incomplete
- No frontend yet; `client/` still holds only `.gitkeep`.
- Backend still only serves `GET /health` over HTTP — the repository works but nothing is routed to it.
- **`Main.kt` lines 29–43 are the throwaway 1.5 test block.** Delete them in 1.6; they insert and delete rows on every single startup.
- 1.5's three modified files are **uncommitted**.
- `server/.kotlin/` is untracked build output — add it to `.gitignore`.
- `create()`'s SQL says `VAlUES` (mixed case). Harmless — SQL keywords are case-insensitive — but worth tidying.

---

## Current Checkpoint

### Last Task Worked On
- **1.5 — Remaining repository methods** — done (2026-09-08, not yet committed). Learned: JDBC parameter indexes follow the `?` order **in the SQL string**, not the function signature — the classic silent-wrong-column bug. `RETURNING *` on an `UPDATE` that matches no row returns zero rows, so `null` falls out of the same query with no separate existence check. `executeUpdate()` returns a row count, so `executeUpdate() > 0` is a free "did it exist?" for `delete`. And nullable/Boolean returns are how the repository reports facts without deciding HTTP policy.

### Current Progress
- [x] `Todo` data class + `CreateTodoRequest` / `UpdateTodoRequest` DTOs.
- [x] `findAll()`, `create(text)`, private `ResultSet.toTodo()`.
- [x] `findById(id): Todo?`, `update(id, text, done): Todo?`.
- [x] `delete(id): Boolean`, `deleteCompleted(): Int`.
- [x] Every method exercised manually against Postgres, hit and miss paths.
- [ ] Commit 1.5.
- [ ] Delete the throwaway block from `Main.kt`.
- [ ] `TodoRoutes.kt` registering the five endpoints.
- [ ] Correct status codes (`201`, `204`, `404`).

### Next Task
**1.6 — REST routes**
- [ ] `GET /api/todos` — list all.
- [ ] `POST /api/todos` — create, respond **201**.
- [ ] `PUT /api/todos/{id}` — update, **404** when `update()` returns null.
- [ ] `DELETE /api/todos/{id}` — delete, respond **204**, **404** when `delete()` returns false.
- [ ] `DELETE /api/todos?done=true` — clear completed.
- [ ] Keep routes thin: parse → call repository → set status. No SQL, no business logic.
- [ ] Group them in a `TodoRoutes.register(app, repository)` function (plainer than Javalin's `apiBuilder`, and one less API to learn).
- **Done when:** all five endpoints work end-to-end against Postgres.

Notes for this task:
- **"Thin routes" means:** the handler's whole job is translating between HTTP and Kotlin. Read the path param, deserialize the body, call one repository method, choose a status code. The moment a handler contains an `if` about *todos* rather than about *the request*, that logic belongs lower down.
- **The status codes and what they mean:** `200` = here's your data. `201 Created` = made something new. `204 No Content` = it worked, and there's deliberately nothing in the body (the right answer for a delete). `404` = no such row.
- **This is where `Todo?` and `Boolean` pay off.** `repository.findById(id) ?: throw NotFoundResponse()` — the elvis operator turns null into a 404. Javalin's `NotFoundResponse` is an exception it catches and converts to a real HTTP 404, so you don't set statuses by hand for error paths.
- **Reading input:** `ctx.pathParam("id").toLong()` for the URL segment, `ctx.bodyAsClass<CreateTodoRequest>()` for the JSON body (this is what uses the Jackson mapper wired up in 1.2). `ctx.queryParam("done")` for the `?done=true` case.
- **Watch the two `DELETE /api/todos` routes** — `DELETE /api/todos/{id}` and `DELETE /api/todos?done=true` are *different paths* (`/todos/{id}` vs `/todos`), so they're two separate route registrations. A query string is not part of the path.
- **`.toLong()` throws on garbage input** like `/api/todos/abc`. `.toLongOrNull()` plus an elvis is the safer form once you care about that.
- **Why `/api/...` and not `/todos`:** the prefix keeps API routes from colliding with frontend routes later, and makes the Vite dev proxy rule trivial (`/api` → `localhost:7070`).
- Pass the repository into `register()` rather than making it global — same dependency-injection reasoning as `TodoRepository(dataSource)`.

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
| `server/build.gradle.kts` | Done through 1.5 — Kotlin JVM 2.3.0 + `application`, toolchain 25, Javalin 6.7.0, slf4j-simple, Jackson (kotlin + jsr310), HikariCP, Postgres JDBC. Task 1.6 needs no new dependency. |
| `server/src/main/kotlin/Main.kt` | Fail-fast DB check, Jackson mapper, `GET /health`. **Lines 29–43 are throwaway 1.5 test code — delete in 1.6.** Task 1.6 replaces them with `TodoRoutes.register(app, todoRepository)`. |
| `server/src/main/kotlin/Config.kt` | Done — env-backed `dbUrl`, `dbUser`, `dbPassword`, `port` with local defaults. |
| `server/src/main/kotlin/Database.kt` | Done — `createDataSource()` builds the `HikariDataSource`; `checkConnection()` runs `SELECT 1`. |
| `server/src/main/kotlin/Todo.kt` | Done — `Todo`, `CreateTodoRequest(text)`, `UpdateTodoRequest(text, done)` (both non-null: PUT contract). |
| `server/src/main/kotlin/TodoRepository.kt` | Done — all SQL lives here: `findAll`, `create`, `findById`, `update`, `delete`, `deleteCompleted`, private `ResultSet.toTodo()`. |
| `server/src/main/kotlin/TodoRoutes.kt` | `TBD` (task 1.6) — the five REST endpoints, thin handlers only. |
| `server/gradlew` | Done — the Gradle wrapper, committed. Nobody needs a system `gradle` install any more. |
| `client/src/` | `TBD` — React entrypoint, TanStack Query client, API layer. |
| `client/vite.config.ts` | `TBD` — dev server + `/api` proxy to the backend. |

### API Surface (as of 1.6)
| Method | Path | Body | Success |
|---|---|---|---|
| GET | `/api/todos` | — | 200, `Todo[]` |
| POST | `/api/todos` | `{text}` | 201, `Todo` |
| PUT | `/api/todos/{id}` | `{text, done}` | 200, `Todo` / 404 |
| DELETE | `/api/todos/{id}` | — | 204 / 404 |
| DELETE | `/api/todos?done=true` | — | 200, count |

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
