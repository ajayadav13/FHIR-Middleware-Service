# FHIR Middleware (Ktor, PostgreSQL, HAPI FHIR R4)

A secure Ktor middleware that manages local user authentication and delegates clinical data storage to the public HAPI FHIR R4 test server. Users can register/login, create Patient resources on the external FHIR server, and track per-user resource counts in PostgreSQL.

## Tech Stack
- Kotlin + Ktor (Netty, Sessions, ContentNegotiation, Swagger/OpenAPI)
- PostgreSQL + Exposed ORM + HikariCP
- HAPI FHIR Client (R4)
- Ktor HTML DSL (server-side rendered pages)
- Docker + Docker Compose (multi-stage build, containerized deployment)


### Data Flows (key journeys)

**Register/Login**
```mermaid
sequenceDiagram
    participant U as User
    participant S as Ktor Server
    participant DB as PostgreSQL
    U->>S: GET /register or /login (HTML)
    U->>S: POST form (email, password)
    S->>DB: insert/find user, bcrypt verify, update last_accessed
    S-->>U: Set session cookie + redirect (/dashboard or /patient/new)
```

**Create Patient**
```mermaid
sequenceDiagram
    participant U as User
    participant S as Ktor Server
    participant DB as PostgreSQL
    participant F as Public HAPI FHIR
    U->>S: POST /patient/new (firstName, lastName, gender)
    S->>F: HAPI client create Patient
    F-->>S: MethodOutcome (resource id)
    S->>DB: increment resources_created, update last_accessed
    S-->>U: Success page + links (/patient/{id})
```

## Project Structure
- `src/main/kotlin/com/example/Application.kt` — Ktor entrypoint, DI wiring, plugins.
- `src/main/kotlin/com/example/routes/AuthRoutes.kt` — Register/Login/Logout pages (HTML).
- `src/main/kotlin/com/example/routes/PatientRoutes.kt` — Dashboard, patient create, search, view.
- `src/main/kotlin/com/example/db/*` — Exposed table, repo, DB factory (Hikari).
- `src/main/kotlin/com/example/fhir/*` — HAPI FHIR client provider and service.
- `src/main/kotlin/com/example/templates/Layout.kt` — Shared HTML layout/styling.
- `src/main/resources/application.conf` — Ktor + DB + FHIR config.
- `src/main/resources/openapi/documentation.yaml` — Swagger spec served at `/swagger`.
- `docker-compose.yml` — PostgreSQL 16 on host port `5433`.

## Prerequisites
- Docker + Docker Compose
- Java 21 (only for local development without Docker)
- Git

## 🚀 Quick Start (Fully Dockerized - Recommended)

This is the simplest way to run the entire application with a single command.

### 1. Clone the Repository
```bash
git clone <repo-url> fhir-service-1.0
cd fhir-service-1.0
```

### 2. Run with Docker Compose
```bash
docker compose up --build
```

This command will:
- Build the application using a multi-stage Docker build (Gradle + JRE)
- Start PostgreSQL container
- Start the application container
- Create a Docker network for service communication
- Set up health checks for both services

### 3. Access the Application
- App: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger

### 4. Stop the Application
```bash
# Stop and remove containers
docker compose down

# Stop and remove containers + volumes (deletes all data)
docker compose down -v
```

### Docker Architecture

```mermaid
graph TD
    U[User Browser :8080] -->|HTTP| App[Ktor App Container]
    App -->|JDBC :5432| DB[(PostgreSQL Container)]
    App -->|HTTPS| FHIR[(Public HAPI FHIR R4)]
    DB ---|Volume Mount| Vol[pgdata volume]
    App ---|Docker Network| Net[fhir-network]
    DB ---|Docker Network| Net
```

The Docker setup includes:
- **Multi-stage build**: Reduces final image size (~200MB vs 1GB+)
  - Stage 1: Gradle build with full JDK
  - Stage 2: Runtime with minimal JRE + JAR only
- **Health checks**: Ensures PostgreSQL is ready before app starts
- **Non-root user**: Application runs as `ktor` user for security
- **Persistent volumes**: Database data survives container restarts
- **Optimized caching**: Gradle dependencies cached for faster rebuilds

### Docker Commands Reference
```bash
# Build and start all services
docker compose up --build -d

# View logs
docker compose logs -f          # all services
docker compose logs -f app      # app only
docker compose logs -f postgres # db only

# Restart a service
docker compose restart app

# Check service status
docker compose ps

# Execute commands in containers
docker compose exec app sh           # shell in app container
docker compose exec postgres psql -U ktor -d fhirmiddleware  # psql in db

# Rebuild only the app (after code changes)
docker compose build app
docker compose up -d app

# Stop services
docker compose stop

# Remove everything (including volumes/data)
docker compose down -v
```

---

## 🛠️ Alternative: Local Development (Docker + Gradle)
If you want to develop locally without containerizing the app (faster iteration), you can run only PostgreSQL in Docker and the app with Gradle.

### 1. Start Only PostgreSQL
```bash
docker compose up -d postgres
```

This starts PostgreSQL on host port **5433** (not 5432).

### 2. Run the Application Locally
```bash
./gradlew run
```

### 3. Access the Application
- App: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger

**Note**: The local config connects to `localhost:5433` by default.

### Default Database Settings (from `application.conf`)
- URL: `jdbc:postgresql://localhost:5433/fhirmiddleware`
- User: `ktor`
- Password: `ktorpass`

You can override via env vars:
```
DATABASE_URL
DATABASE_USER
DATABASE_PASSWORD
DATABASE_POOL_SIZE
```

## Primary Routes & Pages
- `GET /register` — Create account (stores bcrypt hash in Postgres).
- `POST /register`
- `GET /login` — Authenticate and create session cookie.
- `POST /login`
- `GET /logout`
- `GET /dashboard` — Stats (resources_created, last_accessed) and quick actions.
- `GET /patient/new` — Form to create FHIR Patient on public HAPI server.
- `POST /patient/new`
- `GET /patient/search` + `POST /patient/search` — Search by Patient ID.
- `GET /patient/{id}` — Fetch Patient from HAPI FHIR and show raw JSON.
- `GET /swagger` — OpenAPI/Swagger UI (serves `openapi/documentation.yaml`).

## Database Schema
Table `users` (Exposed):
- `email` (PK, varchar 255)
- `password` (hashed, varchar 255)
- `resources_created` (int, default 0)
- `last_accessed` (timestamp)

## Inspecting Data in PostgreSQL

### If Running Fully Dockerized (recommended)
```bash
# Access PostgreSQL from within the container
docker compose exec postgres psql -U ktor -d fhirmiddleware

# Then run SQL commands:
\dt              -- list tables
\d users         -- describe users table
SELECT * FROM users;
\q               -- quit
```

### If Running Locally (only postgres in Docker)
```bash
# Connect from host (port 5433)
psql "postgresql://ktor:ktorpass@localhost:5433/fhirmiddleware"

# Then run SQL commands:
\dt              -- list tables
\d users         -- describe users table
SELECT * FROM users;
\q               -- quit
```

## Troubleshooting

### Docker Issues
- **Port 8080 already in use**: 
  ```bash
  # Find and stop the process using port 8080
  lsof -ti:8080 | xargs kill -9
  # Or change the port in docker-compose.yml: "9090:8080"
  ```

- **Port 5433 already in use** (local dev only):
  ```bash
  docker compose down
  lsof -ti:5433 | xargs kill -9
  docker compose up -d postgres
  ```

- **App container keeps restarting**:
  ```bash
  # Check logs for errors
  docker compose logs app
  
  # Ensure PostgreSQL is healthy
  docker compose ps
  ```

- **Database connection refused in Docker**:
  - Ensure `depends_on` health check is working
  - Check that DATABASE_URL uses `postgres:5432` (not `localhost:5433`)
  - Verify network connectivity: `docker compose exec app ping postgres`

- **Build fails**:
  ```bash
  # Clean build and retry
  docker compose down
  docker compose build --no-cache
  docker compose up
  ```

### Local Development Issues
- **DB connection refused (local mode)**: ensure `docker compose up -d postgres` is running and port 5433 is free.
- **Gradle daemon busy**: rerun with `./gradlew --stop` then `./gradlew run`.
- **Out of memory during build**: Increase Docker memory limit in Docker Desktop settings (recommend 4GB+).

## Environment Variables

The following environment variables can be customized in `docker-compose.yml` or via `.env` file:

| Variable | Default | Description |
|----------|---------|-------------|
| `DATABASE_URL` | `jdbc:postgresql://postgres:5432/fhirmiddleware` | PostgreSQL JDBC URL (use `postgres` hostname in Docker) |
| `DATABASE_USER` | `ktor` | Database username |
| `DATABASE_PASSWORD` | `ktorpass` | Database password |
| `DATABASE_POOL_SIZE` | `10` | HikariCP connection pool size |
| `PORT` | `8080` | Application port |
| `JAVA_OPTS` | `-Xms256m -Xmx512m` | JVM memory settings |

## Production Considerations

Before deploying to production:

1. **Change default credentials**: Update PostgreSQL and database passwords in `docker-compose.yml`
2. **Use secrets management**: Don't hardcode passwords (use Docker secrets or env files)
3. **SSL/TLS**: Configure HTTPS with reverse proxy (nginx, traefik)
4. **Resource limits**: Add CPU/memory limits to docker-compose services
5. **Logging**: Configure structured logging and log aggregation
6. **Monitoring**: Add Prometheus metrics and health check endpoints
7. **Backup**: Set up automated PostgreSQL backups
8. **Security**: Run security scans on Docker images

## Notes
- Clinical data is stored remotely on the public HAPI FHIR R4 server at `https://hapi.fhir.org/baseR4`.
- Sessions are cookie-based; passwords are hashed with BCrypt before storage.
- The Docker image uses Alpine Linux and OpenJDK 21 JRE for minimal footprint.
- Application runs as non-root user `ktor` inside the container for security.
