# Positive Thoughts - Microservices Demo Application

A cloud-native microservices demonstration built on Red Hat OpenShift, Quarkus, and AI integration. The application manages a collection of positive thoughts (quotes) and showcases modern enterprise Java patterns including event-driven architecture, AI-powered content evaluation, and developer portal integration.

Designed for two audiences: **Solutions Architects** running customer demos and **Enterprise Java developers** learning cloud-native development through a progressive workshop.

## Architecture Overview

```
                         +-----------------+
                         |  msa-ai-admin   |
                         |  (Next.js)      |
                         |  Admin CRUD UI  |
                         +-------+---------+
                                 |
                                 v
+------------------+    +-------------------+    +----------------------+
|  msa-ai-frontend | -> | thoughts-msa-ai-  | -> |  Kafka               |
|  (Next.js)       |    | backend (Quarkus) |    |  (AMQ Streams)       |
|  Public UI       |    | REST API + Events |    |  thoughts.events     |
+------------------+    +--------+----------+    +----------+-----------+
                                 |                          |
                                 v                          v
                        +--------+----------+    +----------+-----------+
                        |   PostgreSQL      |    | ai-evaluation-service|
                        |   (OpenShift)     | <- | (Quarkus + LLM)     |
                        +-------------------+    +----------------------+
                                                          |
                                                          v
                                                 +--------+---------+
                                                 |  OpenShift AI    |
                                                 |  (LLM Endpoint)  |
                                                 +------------------+
```

## Microservices

### thoughts-msa-ai-backend (Quarkus)

Core REST API service for managing positive thoughts.

| Endpoint | Method | Description |
|---|---|---|
| `/thoughts` | GET | List all thoughts (paginated, default 20/page) |
| `/thoughts` | POST | Create a new thought |
| `/thoughts/{id}` | GET | Get a thought by UUID |
| `/thoughts/{id}` | PUT | Update a thought |
| `/thoughts/{id}` | DELETE | Delete a thought |
| `/thoughts/random` | GET | Get a random thought |
| `/thoughts/thumbsup/{id}` | POST | Increment thumbs up |
| `/thoughts/thumbsdown/{id}` | POST | Increment thumbs down |

Key features:
- PostgreSQL persistence via Hibernate Panache (UUID primary keys)
- Flyway database migrations (5 migrations)
- Kafka event publishing on create/update/delete (`thoughts.events` topic)
- Input validation (content: 10-500 chars, author/bio: max 200 chars)
- Health checks (database, Kafka, LLM), Prometheus metrics
- 30+ preloaded quotes from notable authors seeded on startup

Data model: `Thought` entity with content, thumbsUp/thumbsDown counters, status (APPROVED/REMOVED/IN_REVIEW), author, authorBio, and timestamps.

### ai-evaluation-service (Quarkus)

Consumes Kafka events and uses an LLM to evaluate thought quality via vector embeddings.

| Endpoint | Method | Description |
|---|---|---|
| `/evaluations` | GET | List all evaluations (paginated) |
| `/evaluations/thought/{thoughtId}` | GET | Get evaluation for a specific thought |
| `/evaluations/stats` | GET | Summary stats (total, approved, rejected, avg score) |
| `/ui/evaluations` | GET | HTML table view of evaluations (Qute templates) |
| `/ui/evaluations/stats` | GET | HTML stats dashboard |

Key features:
- Kafka consumer filters for CREATED events only
- Generates vector embeddings via Langchain4j (OpenAI-compatible endpoint)
- Cosine similarity comparison against stored negative vectors
- Configurable similarity threshold (default 0.85) -- APPROVED if below, REJECTED if above
- SmallRye Fault Tolerance: 2 retries with exponential backoff on LLM calls
- Standalone Qute-based web UI for viewing evaluations and stats
- Health checks for database, Kafka, and LLM endpoint connectivity
- Prometheus metrics: evaluation throughput, success/failure rates, processing time
- 43 tests (entities, Kafka consumer, evaluation logic, REST API, health checks, integration)

### msa-ai-frontend (Next.js)

Public-facing UI for browsing and rating random positive thoughts.

- Fetches and displays a single random thought at a time
- Shows author and biographical attribution
- Thumbs up/down voting (mutually exclusive)
- "View Another Thought" button for browsing
- Gradient background (blue-to-purple), responsive card layout
- Loading skeletons, error handling with toast notifications
- Light/dark theme support

### msa-ai-admin (Next.js)

Administrative interface for managing the thoughts collection.

- **List view**: Table with content, ratings, rating percentage, status badges, pagination
- **Create form**: Content (10-500 chars), author, bio fields with character counters
- **Edit form**: Pre-populated fields, status dropdown (APPROVED/REMOVED/IN_REVIEW), read-only rating stats
- **Delete**: Confirmation dialog before removal
- Built with shadcn/ui, react-hook-form, and Zod validation

## Tech Stack

| Layer | Technology |
|---|---|
| Backend framework | Quarkus 3.31.2 (Java 25) |
| Frontend framework | Next.js (React), compiled to static sites |
| UI components | shadcn/ui (Radix UI + Tailwind CSS) |
| Database | PostgreSQL with Hibernate Panache ORM |
| Migrations | Flyway |
| Messaging | Apache Kafka via Red Hat AMQ Streams |
| AI integration | Langchain4j with OpenShift AI (OpenAI-compatible) |
| Container platform | Red Hat OpenShift |
| Build | Maven (backend), npm (frontend) |
| Testing | JUnit 5, REST Assured, Testcontainers |
| Observability | Micrometer/Prometheus, SmallRye Health, OpenTelemetry |

## Running Locally

### Prerequisites

- Java 25+ and Maven
- Node.js 18+
- Docker or Podman (for Quarkus dev services -- auto-provisions PostgreSQL and Kafka)

### Backend Services

```bash
# Thoughts backend (starts on port 8080 with dev services for DB + Kafka)
cd thoughts-msa-ai-backend
./mvnw quarkus:dev

# AI evaluation service (starts on port 8081)
cd ai-evaluation-service
./mvnw quarkus:dev
```

Quarkus dev services automatically start PostgreSQL and Kafka containers in dev mode.

### Frontend Applications

```bash
# Public frontend (port 3000)
cd msa-ai-frontend
npm install && npm run dev

# Admin frontend (port 3001)
cd msa-ai-admin
npm install && npm run dev
```

### Environment Variables (Production)

| Variable | Description |
|---|---|
| `QUARKUS_DATASOURCE_JDBC_URL` | PostgreSQL connection URL |
| `QUARKUS_DATASOURCE_USERNAME` | Database username |
| `QUARKUS_DATASOURCE_PASSWORD` | Database password |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses |
| `OPENSHIFT_AI_ENDPOINT_URL` | OpenShift AI LLM endpoint |
| `OPENSHIFT_AI_API_KEY` | API key for LLM endpoint |
| `EVALUATION_SIMILARITY_THRESHOLD` | Cosine similarity threshold (default: 0.85) |

## OpenShift Deployment

Kubernetes/OpenShift manifests are included with:
- Resource requests: 256Mi memory, 250m CPU
- Resource limits: 512Mi memory, 500m CPU
- Liveness probe: `/q/health/live` (30s initial delay)
- Readiness probe: `/q/health/ready` (10s initial delay)
- Labels: `app.openshift.io/runtime=quarkus`

## Database Schema

Three tables managed by Flyway migrations:

- **thoughts** -- Core content with ratings and status tracking
- **thought_evaluations** -- AI evaluation results with similarity scores (FK to thoughts)
- **evaluation_vectors** -- Pre-computed positive/negative reference vectors for similarity comparison

## Roadmap Status

### Completed

- [x] **Thoughts Service Backend** -- Quarkus REST API with full CRUD, random thought, and rating endpoints
- [x] **Frontend Application** -- Next.js public UI and admin UI with shadcn/ui
- [x] **Random Thought Display** -- Fetch and display random thoughts with smooth UI
- [x] **Rating System** -- Thumbs up/down with backend persistence
- [x] **Event-Driven Architecture** -- Kafka event publishing on thought create/update/delete
- [x] **AI Evaluation Service** -- Kafka consumer, LLM-based vector evaluation, result persistence, REST API, Qute web UI, health checks, and metrics
- [x] **OpenShift Deployment Manifests** -- Complete K8s/OpenShift configs for all services

### Remaining

- [ ] **PostgreSQL Database Setup** -- Configure PostgreSQL in OpenShift with proper schema (currently runs via dev services locally)
- [ ] **Frontend Service Integration** -- Create Quarkus service to serve static Next.js frontend with CORS/routing config
- [ ] **Kafka Infrastructure** -- Deploy AMQ Streams Operator and configure Kafka cluster in OpenShift with topics
- [ ] **OpenShift AI LLM Setup** -- Configure OpenShift AI to serve LLM model with resource allocation and endpoint config
- [ ] **AI Evaluation UI** -- Extend the Next.js frontend to display AI evaluation results (currently only available via the evaluation service's standalone Qute UI)
- [ ] **Developer Hub Integration** -- Register all microservices in Red Hat Developer Hub with metadata, API docs, and service relationships
- [ ] **Workshop Materials** -- Progressive workshop guide with checkpoints, setup instructions, and validation steps

### Summary

**7 of 14 roadmap items completed (50%).** The core application logic is fully built -- all four microservices are functional with tests passing. The remaining items fall into two categories:

1. **Infrastructure provisioning** (items 1, 4, 7, 9) -- Setting up PostgreSQL, Kafka, and OpenShift AI as managed services in OpenShift rather than relying on dev services
2. **Integration and documentation** (items 4, 11, 12, 14) -- Connecting the frontends to serve from Quarkus, surfacing AI evaluations in the main UI, Developer Hub registration, and writing workshop materials

## Project Structure

```
DevHub/
+-- thoughts-msa-ai-backend/    # Core REST API (Quarkus)
+-- ai-evaluation-service/      # AI evaluation consumer (Quarkus)
+-- msa-ai-frontend/            # Public UI (Next.js)
+-- msa-ai-admin/               # Admin UI (Next.js)
+-- agent-os/                   # Agent OS configuration
    +-- product/                # Mission, roadmap, tech stack docs
    +-- specs/                  # Feature specifications
    +-- standards/              # Coding standards by domain
    +-- config.yml              # Agent OS settings
```
