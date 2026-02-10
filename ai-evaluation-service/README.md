# AI Evaluation Service

A Quarkus microservice that consumes thought-created events from Kafka, generates vector embeddings using Langchain4j and OpenShift AI, evaluates thoughts against predefined positive and negative vectors, and stores evaluation results in PostgreSQL.

## Features

- Kafka consumer for thought-created events
- Langchain4j integration for vector embeddings via OpenShift AI
- Vector similarity comparison using cosine similarity
- Automatic thought approval/rejection based on similarity thresholds
- PostgreSQL storage for evaluation results and predefined vectors
- REST API for retrieving evaluations and statistics
- Web UI for displaying evaluation results
- Health checks and Prometheus metrics
- Retry logic with fault tolerance for LLM failures

## Tech Stack

- Quarkus 3.31.2
- Java 25
- Langchain4j (Quarkus extension)
- PostgreSQL with Hibernate Panache
- Kafka with SmallRye Reactive Messaging
- Qute templating for web UI
- SmallRye Fault Tolerance for retry logic
- Micrometer with Prometheus for observability

## Configuration

Key configuration properties in `application.properties`:

- `evaluation.similarity.threshold` - Cosine similarity threshold for rejection (default: 0.85)
- `quarkus.langchain4j.openai.base-url` - OpenShift AI embedding model endpoint
- `kafka.bootstrap.servers` - Kafka broker connection
- Database connection properties for PostgreSQL

## Running the Application

### Development Mode

```bash
./mvnw compile quarkus:dev
```

### Production Mode

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Running Tests

```bash
./mvnw test
```

## Endpoints

### REST API

- `GET /evaluations` - List all evaluations (paginated)
- `GET /evaluations/thought/{thoughtId}` - Get evaluation for specific thought
- `GET /evaluations/stats` - Get evaluation statistics

### Web UI

- `GET /ui/evaluations` - View evaluations table
- `GET /ui/evaluations/stats` - View evaluation statistics dashboard

### Health & Metrics

- `GET /q/health/live` - Liveness probe
- `GET /q/health/ready` - Readiness probe
- `GET /q/metrics` - Prometheus metrics

## Architecture

The service follows these patterns:

1. Kafka consumer receives thought-created events
2. Embedding service generates vector for thought content
3. Similarity service compares thought vector against negative vectors
4. Evaluation service determines APPROVED or REJECTED status
5. Result is persisted to database with transaction
6. REST API exposes evaluation data
7. Web UI displays results

## Database Schema

- `evaluation_vectors` - Stores predefined positive and negative vectors
- `thought_evaluations` - Stores evaluation results for each thought

## Development

Built with Red Hat Developer Hub and OpenShift AI integration.

For more information, see the specification document in the project repository.
