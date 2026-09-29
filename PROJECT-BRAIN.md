# PROJECT-BRAIN

> Fullstack todo list app — Kotlin API + React client. Read this file first when returning.
> **Last updated:** 2026-09-29

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
- [x] **1.8 — CORS, logging, API check** done: `config.bundledPlugins.enableCors { cors -> cors.addRule { it.allowHost("http://localhost:5173") } }` and `config.requestLogger.http { ctx, ms -> ... }` both added to the `Javalin.create { }` block. Verified: the `Access-Control-Allow-Origin` header appears for the 5173 origin and is absent for any other. A Postman collection replaced the planned `api.http` — six requests (GET, POST, PUT, DELETE by id, DELETE `?done=true`, blank-text 400), run top to bottom via the collection runner, self-cleaning so the DB ends where it started.
- [x] **Both 1.7 leftovers fixed:** thrown 404s keep their message, and a non-numeric id returns 400 instead of 500. All checks re-run and passing.
- [x] **1.9 — Backend tests** done, deliberately trimmed. `build.gradle.kts` gained JUnit 5 (via `junit-bom:5.11.4`), `javalin-testtools:6.7.0`, `junit-platform-launcher`, and `tasks.test { useJUnitPlatform() }`. `Main.kt` now exposes `createApp(repository): Javalin` — builds and configures but does **not** start — and `main()` is down to the DB check plus `createApp(TodoRepository(dataSource)).start(Config.port)`. `src/test/kotlin/TodoApiTest.kt` holds four tests: GET on an empty table, POST creates (checked in the response *and* in the DB), blank text → 400 with nothing written, unknown id → 404. `./gradlew test` green.
- [x] **Update contract decided:** `PUT /todos/{id}` sends **both** `text` and `done`. `UpdateTodoRequest` fields are therefore non-nullable, so `jackson-module-kotlin` rejects a request missing either one — the type declaration *is* the validation.
- [x] **2.1 — Vite + React 19 + TS scaffold** done (`a1fdb16`): `create-vite` 9.2.1 `react-ts` template in `/client`, boilerplate stripped (`App.tsx` returns `null`, `index.css` empty, only `public/favicon.svg` kept). `"strict": true` confirmed in `tsconfig.app.json`. `@/` → `src/` alias in both `tsconfig.app.json` (`paths`) and `vite.config.ts` (`resolve.alias` via `fileURLToPath`). `server.proxy` forwards `/api` to `http://localhost:7070`. Both `.gitkeep` placeholders removed.
- [x] **2.2 — Tailwind v4 + theme tokens** done (`29a3c7f`): `tailwindcss` + `@tailwindcss/vite` 4.3.3 installed, `tailwindcss()` added to `plugins` next to `react()`. `index.css` = `@import "tailwindcss";` + an `@theme` block with the six mockup colors (`--color-bg`, `-surface`, `-border`, `-text`, `-muted`, `-accent`) and `--font-sans: "Inter", ui-sans-serif, system-ui, sans-serif`. Inter 400/500/600 loaded via Google Fonts `<link>` tags in `index.html`. Verified with a throwaway test div (all six tokens + DevTools *Rendered Fonts* = Inter), then removed — `App.tsx` is back to `return null`.

### What Is In Progress
- **2.3 — Layout shell** — next up. Backend done through 1.9, client scaffold (2.1) and styling setup (2.2) done.

### What Is Broken / Incomplete
- The client renders a blank page — no UI yet, by design until 2.3.
- `tailwindcss` and `@tailwindcss/vite` landed in `dependencies` rather than `devDependencies`. Harmless — Vite bundles everything either way — but build tools conventionally go in `devDependencies`. Optional cleanup.
- `tsconfig.app.json` uses `"baseUrl": "."` plus `"ignoreDeprecations": "6.0"` to silence TypeScript 6's `baseUrl` deprecation. Works, but `baseUrl` isn't needed — `"@/*": ["./src/*"]` alone resolves relative to the tsconfig. Optional cleanup.
- **Test coverage is partial by choice.** Four tests, not one per endpoint: PUT, the `?done=true` bulk delete, and the 500-char cap are untested. Enough to catch real breakage and learn the mechanics; extend if the backend starts changing often.
- **Tests share the dev database.** `@BeforeEach` runs `TRUNCATE todos RESTART IDENTITY`, so `./gradlew test` wipes local todos and cannot run while something else is using the table. A separate `todos_test` database is the fix if this ever bites.
- **The Postman collection lives only in the Postman app**, by choice — it is not in git and would vanish with a reinstall. Accepted, because 1.9 replaces it with real tests.
- Error message capitalization is inconsistent (`"Text must not be blank"` vs `"id must be a number"`). Cosmetic.

---

## Current Checkpoint

### Last Task Worked On
- **2.2 — Tailwind v4 + theme tokens** — done (2026-09-29, `29a3c7f`). Learned: v4 is configured **in CSS** (`@theme`), not in a `tailwind.config.js`. **Token names become class names** — the `--color-` prefix marks a color, so `--color-bg` generates `bg-bg`, `text-bg`, `border-bg`, … and `--font-sans` becomes the page's default font. **Loading a font and using it are separate steps**: the `<link>` in `index.html` downloads Inter, and `--font-sans` applies it. Because a fallback stack (`system-ui`) looks nearly identical, check DevTools → Computed → **Rendered Fonts** rather than trusting your eyes. `git restore <file>` throws away one file's uncommitted changes — always name the file, never `.`, or it wipes real work too.
- **2.1 — Vite + React 19 + TS scaffold** — done (2026-09-25, `a1fdb16`). Learned: the `@/` alias has to be declared **twice** because two different tools read the code — TypeScript (`paths` in `tsconfig.app.json`) only type-checks, while Vite (`resolve.alias`) actually bundles; declare it in one and the other breaks. There are **two tsconfigs** because the code runs in two places: `tsconfig.app.json` for `src/` (browser), `tsconfig.node.json` for `vite.config.ts` (Node). The **dev proxy makes CORS irrelevant in development**: the browser only ever talks to `localhost:5173`, and Vite forwards `/api/*` to 7070 behind the scenes — which is why `fetch` calls must use a **relative** path like `/api/todos`.
- **1.9 — Backend tests (trimmed)** — done (2026-09-21). Scope was cut deliberately: no separate test database, no test per endpoint. Learned: **`testImplementation` and `implementation` are separate classpaths** — test code sees the app, the app never sees the tests. A **BOM** (`platform("org.junit:junit-bom:5.11.4")`) pins one version across a family of artifacts so their pieces cannot drift. Gradle still defaults to JUnit 4, so without `useJUnitPlatform()` it finds zero tests and reports **BUILD SUCCESSFUL** — "all passed" and "nothing ran" look identical, and the same trap catches a `@Test` annotation left off by accident; read the test count, not the colour. `@BeforeEach` runs per test method, not per class, which is what keeps tests independent — and JUnit builds a **new instance of the test class for every test**, so shared setup like the connection pool belongs in a `companion object` or you leak one pool per test. `JavalinTest.test(createApp(repo)) { _, client -> }` starts the app on a random free port and shuts it down afterwards, which is the entire reason `createApp` had to stop calling `.start()`. Assert on the **database as well as the response** — an endpoint can return a convincing 201 while writing nothing. Also: Gradle's up-to-date caching means a fast green build may not have run anything; `--rerun-tasks` forces it.
- **1.8 — CORS, logging, API check** — done (2026-09-19). Learned: **CORS is a browser rule, not a server rule** — curl never cared, which is why nothing broke before the frontend existed; the server's only job is to send `Access-Control-Allow-Origin`, and a browser refuses to hand the response to the requesting JavaScript when that header is missing. Name the origin instead of `anyHost()`. Javalin's request pipeline runs **BEFORE → HTTP → AFTER_MATCHED → ERROR → AFTER**, and that ordering is the whole 404 bug: exception handlers fire in the HTTP stage, `app.error(code)` fires in the later ERROR stage and overwrites whatever body was already written. So the fix was *removing* `app.error(404)`, not adding a handler alongside it. One `app.exception(NotFoundResponse::class.java)` covers both cases, because Javalin throws `EndpointNotFound` for unmatched routes and that class **extends `NotFoundResponse`** — the same nearest-match-up-the-hierarchy resolution that stops the catch-all on `Exception` swallowing the specific handlers. Also: `toLong()` throws, `toLongOrNull()` returns null — prefer the `OrNull` form for anything arriving from outside the program, so *you* decide what bad input means. And a restart lesson: `./gradlew run` compiles once and runs that build; editing the source afterwards changes nothing until Ctrl+C and re-run. The first round of checks failed purely because the old JVM was still up.
- **1.7 — Validation + error handling** — done and committed (2026-09-16, `b24c760`). Learned: there are **two** layers of safety net and they catch different things — `app.exception(X)` fires when an exception escapes a handler, `app.error(code)` fires when the *response status* ends up at `code`, including when no route matched and nothing was ever thrown. Javalin resolves exception handlers by walking the class hierarchy nearest-match-first, so a catch-all on `Exception` does not swallow the specific `ValidationException` handler. A custom exception was worth defining because Javalin's `BadRequestResponse` renders its own JSON shape and the API had promised `{"error": ...}`. And the security habit: `logger.error(msg, e)` puts the stack trace in *your* logs while the client gets a generic message — a leaked trace advertises your library versions and file paths.

### Current Progress
- [x] 2.2 committed (`29a3c7f`).
- [ ] If not done yet: run 2.1's check — with the backend up, `await (await fetch('/api/todos')).json()` in the browser console on 5173 returns an array. Proves the proxy works end to end. (Becomes unavoidable in 2.4 anyway.)
- [ ] Leftover from 1.9: add a "Tests" section to `README.md` — `./gradlew test`, plus the warning that it truncates the dev table.

### Next Task
**2.3 — Layout shell**
- [ ] `App.tsx`: full-height centered column, `max-w-[520px]`, `py-16 px-5`.
- [ ] `<header>` with the "Tasks" h1 (28px, weight 600, `-0.02em` tracking) and the summary paragraph below it — hardcode the summary text for now.
- **Done when:** side-by-side with the mockup, spacing and type look right.

Notes for this task — the mockup's inline styles translated to Tailwind:
| Mockup (`ui-design/todo-app.html`) | Tailwind |
|---|---|
| outer: `min-height:100vh; background; color; display:flex; justify-content:center; padding:64px 20px` | `min-h-screen bg-bg text-text flex justify-center py-16 px-5` |
| inner: `width:100%; max-width:520px` | `w-full max-w-[520px]` |
| header: `margin-bottom:32px` | `mb-8` |
| h1: `margin:0 0 6px; font-size:28px; font-weight:600; letter-spacing:-0.02em` | `mb-1.5 text-[28px] font-semibold tracking-[-0.02em]` |
| summary p: `font-size:14px; color:muted` | `text-sm text-muted` |
- **Two wrappers, two jobs.** The outer div fills the screen and centers; the inner div caps the width. One element can't do both, because `max-w` on a full-screen element would shrink the background too.
- **Square brackets = arbitrary value.** `max-w-[520px]` / `text-[28px]` are for exact values that aren't on Tailwind's scale. Prefer scale classes (`mb-8`, `text-sm`) when they match exactly — they only do here because 32px = `8 × 4px` and 14px = `text-sm`.
- Tailwind's reset (Preflight) already zeroes default `margin` on `h1`/`p`, so the mockup's `margin:0` needs no class.
- `font-sans` isn't needed on the wrapper — `--font-sans` in `@theme` already makes Inter the page default.

### Then
**2.4 — Types + API client**
- [ ] `src/types.ts` mirroring the backend `Todo` shape.
- [ ] `src/api/todos.ts` with `listTodos`, `createTodo`, `updateTodo`, `deleteTodo`, `clearCompleted` — plain `fetch`, each throwing on non-OK responses.
- [ ] One shared `request()` helper that handles JSON parsing, 204s, and error extraction.
- **Done when:** each function is typed and returns real data when called from the console.
- Reminders: `fetch` does **not** throw on 4xx/5xx — only on network failure — so check `res.ok` yourself. The backend's errors are `{"error": "..."}`; `DELETE /{id}` returns 204 with **no body**, so calling `.json()` on it throws. `updateTodo` must send **both** `text` and `done` (the PUT contract). Use relative paths (`/api/todos`) so the Vite proxy handles them.

**2.5 — TanStack Query setup + list rendering**
- [ ] Install, wrap the app in `QueryClientProvider`, add the devtools.
- [ ] `useTodos()` hook wrapping `useQuery({ queryKey: ['todos'], queryFn: listTodos })`.
- [ ] Render the list container (`flex flex-col gap-2`) plus the loading and empty states — the mockup's empty state is centered, muted, 48px vertical padding.
- **Done when:** rows from the database appear in the UI, and an empty table shows "No tasks yet."
- **TanStack Query owns server state.** Do not copy todos into `useState` — the cache is the source of truth. Later: `useMutation` plus `invalidateQueries` for create/update/delete.

**2.6+ — Remaining flows:** create, toggle, edit, delete, clear-completed — matching `ui-design/todo-app.html`.

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
| `README.md` | Written; still missing a Tests section — add `./gradlew test` and the note that it truncates the dev `todos` table. |
| `docker-compose.yml` | Done — `postgres:17` service `db`, volume `todo-pgdata`, port 5432, `schema.sql` init mount. |
| `schema.sql` | Done — the `todos` table (`id` BIGSERIAL, `text` TEXT, `done` BOOLEAN, `created_at` TIMESTAMPTZ). Edits require `docker compose down -v`. |
| `server/build.gradle.kts` | Done through 1.9 — Kotlin JVM 2.3.0 + `application`, toolchain 25, Javalin 6.7.0, slf4j-simple, Jackson (kotlin + jsr310), HikariCP, Postgres JDBC. 1.8 added no dependency (CORS is bundled); 1.9 added JUnit 5 via `junit-bom:5.11.4`, `javalin-testtools:6.7.0`, `junit-platform-launcher` and `tasks.test { useJUnitPlatform() }`. |
| `server/src/main/kotlin/Main.kt` | Done through 1.8 — fail-fast DB check, Jackson mapper, CORS + request logger in the `create { }` block, three `app.exception` handlers (`ValidationException` → 400, `NotFoundResponse` → 404, `Exception` → logged 500), `GET /health`, `TodoRoutes.register(...)`. No `app.error` handlers — they run last and overwrite. Since 1.9 all of that lives in `createApp(repository): Javalin`, which does not start the server; `main()` just does the DB check and `.start(Config.port)`. |
| `server/src/main/kotlin/Config.kt` | Done — env-backed `dbUrl`, `dbUser`, `dbPassword`, `port` with local defaults. |
| `server/src/main/kotlin/Database.kt` | Done — `createDataSource()` builds the `HikariDataSource`; `checkConnection()` runs `SELECT 1`. |
| `server/src/main/kotlin/Todo.kt` | Done — `Todo`, `CreateTodoRequest(text)`, `UpdateTodoRequest(text, done)` (both non-null: PUT contract), `ValidationException`. |
| `server/src/main/kotlin/TodoRepository.kt` | Done — all SQL lives here: `findAll`, `create`, `findById`, `update`, `delete`, `deleteCompleted`, private `ResultSet.toTodo()`. |
| `server/src/main/kotlin/TodoRoutes.kt` | Done (1.6-1.8) — `register(app, repository)` with the five REST endpoints plus the private `validateText()` / `MAX_TEXT_LENGTH`. Path ids parsed with `toLongOrNull()`. Handlers stay thin. |
| `server/gradlew` | Done — the Gradle wrapper, committed. Nobody needs a system `gradle` install any more. |
| *(Postman collection)* | The six-request API check for 1.8. Lives in the Postman app only, deliberately not in the repo — superseded by the 1.9 tests. |
| `server/src/test/kotlin/TodoApiTest.kt` | Done (1.9) — four tests against `createApp(repository)` via `JavalinTest`, `TRUNCATE` in `@BeforeEach`, shared pool + Jackson mapper in a `companion object`. Run with `./gradlew test`. |
| `client/src/` | `main.tsx` entrypoint (imports `index.css`), `App.tsx` returns `null` — 2.3 builds the layout shell here. 2.4 adds `types.ts` and `api/todos.ts`; 2.5 the TanStack Query setup + `useTodos()`. |
| `client/src/index.css` | Done (2.2) — `@import "tailwindcss";` + `@theme` with the six color tokens and `--font-sans` (Inter). New design tokens go here. |
| `client/vite.config.ts` | Done (2.2) — `react()` + `tailwindcss()` plugins, `@` alias, `/api` proxy to `localhost:7070`. |
| `client/tsconfig.app.json` | Done (2.1) — strict mode + `@/*` path mapping (currently via `baseUrl` — see *Broken / Incomplete*). |
| `client/index.html` | Done (2.2) — title "Todo List", Google Fonts preconnect + Inter 400/500/600 `<link>` tags. |

### API Surface (as of 1.7)
| Method | Path | Body | Success |
|---|---|---|---|
| GET | `/api/todos` | — | 200, `Todo[]` |
| POST | `/api/todos` | `{text}` | 201, `Todo` |
| PUT | `/api/todos/{id}` | `{text, done}` | 200, `Todo` / 404 |
| DELETE | `/api/todos/{id}` | — | 204 / 404 |
| DELETE | `/api/todos?done=true` | — | 200, count |

**Error contract (done, 1.7-1.8):** every failure responds with `{"error": "<readable message>"}` — `400` for validation failures *and* a non-numeric id, `404` for unknown ids *and* unknown routes (each keeping its own message), `500` generic for anything unexpected (real stack trace goes to the server log only). Text is trimmed and capped at 500 characters.

### Ports
| Service | Port |
|---|---|
| Postgres | 5432 |
| Kotlin API | 7070 |
| Vite dev server | 5173 — proxies `/api/*` to 7070 (also allowed by CORS since 1.8) |

---

## Quick Resume Guide

1. Read this file top to bottom (~1 min).
2. `git log --oneline -5` and `git status` — confirm reality matches the checkpoint above.
3. Jump to **Current Checkpoint → Next Task** and start on the first unchecked box.
4. Start Docker Desktop (`open -a Docker`), then the database from the repo root: `docker compose up -d`.
   Check the table: `docker compose exec db psql -U todo -d todo -c '\d todos'`
   Wipe it clean: `docker compose exec db psql -U todo -d todo -c 'TRUNCATE todos RESTART IDENTITY;'`
5. Start backend: `./gradlew run` from `/server`. Check: `curl localhost:7070/health` → `{"status":"ok"}`
6. Start frontend: `npm run dev` from `/client` → `http://localhost:5173`. The backend (step 5) must be running too, or `/api` calls fail with a proxy error.
7. **Before ending the session:** update *Current Progress*, *Next Task*, *What's Blocking Me*, and the *Last updated* date.
