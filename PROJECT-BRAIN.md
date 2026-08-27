# PROJECT-BRAIN

> Fullstack todo list app — Kotlin API + React client. Read this file first when returning.
> **Last updated:** 2026-08-27

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
| Runtime | JDK 25 |
| Web framework | Javalin 6 |
| Build tool | Gradle (Kotlin DSL) |
| JSON | Jackson + `jackson-module-kotlin` |
| DB access | HikariCP + JDBC (no ORM) |
| Database | PostgreSQL (via Docker Compose) |
| Schema | `schema.sql` (hand-written, no migrations tool) |

**Repo:** `github.com/Joestarnova/todo-list-app` (`origin` already set)

---

## Current State

### What Currently Works
- [x] Git repo initialized, `origin` remote configured, 1 commit (`a92fa47 initial commit`).
- [x] UI mockup exists: `ui-design/todo-app.html`.
- _e.g. `GET /todos` returns seeded rows_

### What Is In Progress
- [ ] **0.1 — Repo skeleton** (see Current Checkpoint below).
- _e.g. wiring the client fetch layer to the API_

### What Is Broken / Incomplete
- Nothing runs yet — no backend, no frontend, no build files.
- `client/` is empty.
- `server/` contains only `.idea/` (IDE files, untracked and not ignored).
- No root `README.md`.
- No `docker-compose.yml`.
- `.gitignore` only has `.DS_Store` — missing `build/`, `.gradle/`, `node_modules/`, `.env`, `.idea/`.

---

## Current Checkpoint

### Last Task Worked On
- Created the repo, ran `git init`, pushed the initial commit with the UI mockup (2026-08-26).

### Current Progress
- [x] Repo created and pushed to GitHub.
- [x] `client/` and `server/` folders exist (both effectively empty).
- [ ] Folder layout finalized.
- [ ] `.gitignore` covers JVM + Node.
- [ ] Root `README.md` written.
- [ ] `docker-compose.yml` stub added.

### Next Task
**0.1 — Repo skeleton**
- [ ] Confirm folder layout: `/server` (Kotlin), `/client` (React), root `README.md`, `docker-compose.yml`.
- [ ] Expand `.gitignore`: `build/`, `.gradle/`, `node_modules/`, `.env`, `.idea/`.
- [ ] Write a short README — what the app is + how to run each half (leave commands as TODO).
- [ ] Commit and push.
- **Done when:** empty repo pushed with the folder structure in place.

### What's Blocking Me
- Nothing hard-blocking. Stack is decided.
- Check before 0.2: JDK 25 installed and `JAVA_HOME` pointing at it; Docker running.
- _e.g. waiting on Docker Desktop install_

---

## Important Context

### Important Files
| File | Why it matters |
|---|---|
| `PROJECT-BRAIN.md` | This file. Update it before ending each session. |
| `ui-design/todo-app.html` | The visual target for the React client. |
| `.gitignore` | Currently incomplete — part of task 0.1. |
| `README.md` | `TBD` — run instructions for both halves. |
| `docker-compose.yml` | `TBD` — Postgres service for local dev. |
| `server/build.gradle.kts` | `TBD` — Javalin, Jackson, HikariCP, Postgres driver deps. |
| `server/src/main/kotlin/` | `TBD` — `Main.kt` (Javalin app + routes), DB wiring. |
| `server/src/main/resources/schema.sql` | `TBD` — table definitions, applied by hand. |
| `client/src/` | `TBD` — React entrypoint, TanStack Query client, API layer. |
| `client/vite.config.ts` | `TBD` — dev server + `/api` proxy to the backend. |

---

## Quick Resume Guide

1. Read this file top to bottom (~1 min).
2. `git log --oneline -5` and `git status` — confirm reality matches the checkpoint above.
3. Jump to **Current Checkpoint → Next Task** and start on the first unchecked box.
4. Start the database: `docker compose up -d` (root) — `TBD` until compose exists.
5. Start backend: `./gradlew run` from `/server` — `TBD` until Gradle is set up.
6. Start frontend: `npm run dev` from `/client` — `TBD` until Vite is scaffolded.
7. **Before ending the session:** update *Current Progress*, *Next Task*, *What's Blocking Me*, and the *Last updated* date.
