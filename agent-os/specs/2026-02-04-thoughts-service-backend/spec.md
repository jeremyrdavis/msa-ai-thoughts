# Specification: Thoughts Service Backend

## Goal
Build a cloud-native Quarkus microservice for managing positive thoughts with CRUD operations, rating functionality, and event-driven architecture using Kafka, deployable on Red Hat OpenShift.

## User Stories
- As a user, I want to create, read, update, and delete positive thoughts so that I can maintain a collection of uplifting messages
- As a user, I want to rate thoughts with thumbs up or thumbs down so that I can express my reaction to each thought
- As a user, I want to retrieve a random thought so that I can be inspired by unexpected positive messages

## Specific Requirements

**Thought Entity with UUID Identity**
- Use `PanacheEntityBase` instead of `PanacheEntity` to support custom UUID primary key
- Annotate UUID field with `@GeneratedValue(generator = "UUID")` and `@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")`
- Include `String content` field for the thought text
- Include `int thumbsUp` and `int thumbsDown` fields for tracking user reactions
- Include `LocalDateTime createdAt` and `LocalDateTime updatedAt` timestamp fields with automatic population via Panache lifecycle hooks
- Mark entity with `@Entity` annotation and extend `PanacheEntityBase`

**REST API Endpoints with JAX-RS**
- Create `POST /thoughts` endpoint to accept JSON request body with thought content and return created thought with HTTP 201 status
- Create `GET /thoughts/{id}` endpoint to retrieve a single thought by UUID, returning HTTP 404 if not found
- Create `GET /thoughts` endpoint to list all thoughts with pagination support using query parameters `page` and `size` with defaults of page 0 and size 20
- Create `PUT /thoughts/{id}` endpoint to update thought content, validating input and returning updated thought
- Create `DELETE /thoughts/{id}` endpoint to remove a thought, returning HTTP 204 on success
- Create `GET /thoughts/random` endpoint to retrieve a random thought using Panache query method
- Create `POST /thoughts/thumbsup/{id}` endpoint to atomically increment thumbsUp counter and return updated thought
- Create `POST /thoughts/thumbsdown/{id}` endpoint to atomically increment thumbsDown counter and return updated thought

**Input Validation with Hibernate Validator**
- Apply `@NotBlank` annotation to thought content field to reject empty or whitespace-only input
- Apply `@Size(min = 10, max = 500)` annotation to enforce character length constraints on content
- Use `@Valid` annotation on request body parameters in REST endpoints to trigger validation
- Return HTTP 400 status with field-specific error messages when validation fails
- Configure Quarkus validation extension via `quarkus-hibernate-validator` dependency

**Kafka Event Publishing**
- Configure SmallRye Reactive Messaging to publish to `thoughts.events` Kafka topic
- Inject `@Channel("thoughts-events") Emitter<Thought>` in the REST resource or service layer
- Publish complete Thought entity as JSON after successful create, update, and delete operations
- Configure Kafka connector properties using environment variables for broker URL and topic configuration
- Use async fire-and-forget pattern for event publishing to avoid blocking REST responses

**Database Configuration and Persistence**
- Configure PostgreSQL datasource using environment variables: `QUARKUS_DATASOURCE_JDBC_URL`, `QUARKUS_DATASOURCE_USERNAME`, `QUARKUS_DATASOURCE_PASSWORD`
- Enable Hibernate automatic schema generation in dev mode with `quarkus.hibernate-orm.database.generation=drop-and-create`
- Use production-appropriate migration strategy with environment variable override capability
- Configure connection pooling with sensible defaults for OpenShift deployment
- Use Panache active record pattern with static methods like `listAll()`, `findById()`, and custom queries

**Observability and Cloud-Native Features**
- Add `quarkus-smallrye-openapi` dependency to auto-generate OpenAPI 3.0 specification at `/q/openapi`
- Add `quarkus-smallrye-health` dependency to expose `/q/health/live` and `/q/health/ready` endpoints for Kubernetes probes
- Add `quarkus-micrometer-registry-prometheus` dependency to expose metrics at `/q/metrics` for Prometheus scraping
- Configure JSON-formatted logging using `quarkus.log.console.json=true` for log aggregation in OpenShift
- Add custom health check to verify database connectivity and Kafka broker availability

**Maven Dependencies and Build Configuration**
- Use Quarkus platform BOM version 3.31.2 or newer for dependency management
- Include `quarkus-hibernate-orm-panache`, `quarkus-rest-jackson`, `quarkus-jdbc-postgresql` for persistence layer
- Include `quarkus-smallrye-reactive-messaging-kafka` for Kafka integration
- Include `quarkus-hibernate-validator` for input validation
- Include `quarkus-kubernetes` for generating OpenShift deployment manifests
- Include `quarkus-container-image-jib` or `quarkus-container-image-docker` for container image building
- Configure JaCoCo plugin for code coverage reporting in CI/CD pipeline

**Error Handling and Response Formatting**
- Implement centralized exception mapper for validation errors returning structured JSON error responses
- Map `ConstraintViolationException` to HTTP 400 with field-level error details
- Map entity not found scenarios to HTTP 404 with clear error messages
- Map database constraint violations to HTTP 409 or 422 with user-friendly messages
- Log errors with correlation IDs for traceability in distributed environment

**Testing Strategy**
- Use `@QuarkusTest` annotation for integration tests with real database and Kafka using Testcontainers
- Create REST Assured tests for all endpoints covering happy path scenarios
- Test pagination behavior for list endpoint with varying page sizes
- Test random endpoint returns valid thoughts and handles empty database case
- Test rating endpoints correctly increment counters and handle concurrent requests
- Mock or use embedded Kafka for event publishing verification in tests

**OpenShift Deployment Readiness**
- Configure Kubernetes resources via `application.properties` with `quarkus.kubernetes.deployment-target=openshift`
- Define resource limits and requests for CPU and memory appropriate for microservice workload
- Configure liveness probe to `/q/health/live` with appropriate initial delay and period
- Configure readiness probe to `/q/health/ready` to delay traffic until database and Kafka are available
- Use ConfigMaps for non-sensitive configuration and Secrets for database credentials
- Generate OpenShift-compatible container images with proper labels and metadata

## Visual Design
No visual mockups provided - this is a backend API service.

## Existing Code to Leverage

**msa-ai-backend Project Structure**
- Use existing Maven POM structure as template including Quarkus platform BOM configuration
- Follow package naming convention `com.redhat.demos` for consistency with existing projects
- Leverage existing Quarkus test configuration with JUnit 5 and REST Assured setup
- Copy Maven compiler plugin configuration with parameter compilation enabled
- Reuse Surefire and Failsafe plugin configurations for test execution

**Panache Entity Pattern**
- Reference MyEntity.java as baseline for entity structure using Panache
- Extend pattern from `PanacheEntity` to `PanacheEntityBase` for custom UUID primary key
- Use active record pattern with static methods like `listAll()`, `find()`, `persist()`, and `delete()`
- Leverage Panache automatic transaction management in REST endpoints
- Follow pattern of public fields for entity properties as shown in MyEntity

**JAX-RS REST Resource Pattern**
- Use GreetingResource.java as template for REST endpoint structure with `@Path`, `@GET`, `@POST` annotations
- Follow pattern of injecting Panache entities directly or using service layer for business logic
- Apply `@Produces(MediaType.APPLICATION_JSON)` and `@Consumes(MediaType.APPLICATION_JSON)` for JSON handling
- Use JAX-RS Response builder for returning appropriate HTTP status codes
- Structure endpoints as resource-oriented RESTful APIs following existing conventions

**Quarkus Configuration Patterns**
- Reference application.properties for standard Quarkus configuration structure
- Use environment variable overrides with `%prod.` profile prefix for production settings
- Follow existing pattern of minimal dev configuration with production overrides
- Leverage Quarkus dev services for local PostgreSQL and Kafka during development
- Configure datasource, Kafka, and observability extensions following Quarkus documentation

**Maven Build and Dependency Management**
- Copy dependency versions and BOM structure from msa-ai-backend pom.xml
- Use same compiler plugin version and configuration for Java 17+ compilation
- Include JaCoCo plugin configuration for code coverage as shown in existing project
- Follow existing pattern of Quarkus platform artifact imports in dependencyManagement
- Use same test dependency structure with quarkus-junit and rest-assured scoped to test

## Out of Scope
- User authentication and authorization mechanisms including JWT tokens or OAuth integration
- Thought categories, tags, or any classification/taxonomy features for organizing thoughts
- Search functionality including full-text search or filtering by content or metadata
- Advanced querying capabilities beyond basic CRUD and random selection
- User profiles or tracking which user created or rated specific thoughts
- Commenting or discussion features on thoughts
- Sharing thoughts via social media or external platforms
- Email notifications or any notification system
- Rate limiting or throttling on API endpoints
- Caching layer for frequently accessed thoughts
- Soft delete functionality - only hard deletes are implemented
