# todo-list-app

A fullstack todo list app: create, complete, and delete tasks.

## Layout

| Path | What it is |
|---|---|
| `/server` | Kotlin 2.x + Javalin 6 REST API (Gradle Kotlin DSL, JDK 25) |
| `/client` | React 19 + TypeScript SPA (Vite, Tailwind CSS v4, TanStack Query) |
| `/ui-design` | Static UI mockup |
| `docker-compose.yml` | PostgreSQL for local development |
| `PROJECT-BRAIN.md` | Current state, checkpoint, and resume guide |

## Running locally

### Database

```bash
docker compose up -d
```

Postgres listens on `localhost:5432` (db `todo`, user `todo`, password `todo`).

### Backend

From `/server`:

```bash
# TODO: fill in once Gradle is set up
```

### Frontend

From `/client`:

```bash
# TODO: fill in once Vite is scaffolded
```

## Status

Early setup — see `PROJECT-BRAIN.md` for what works and what's next.
