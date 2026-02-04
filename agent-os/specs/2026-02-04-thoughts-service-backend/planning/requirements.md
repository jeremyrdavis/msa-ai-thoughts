# Spec Requirements: Thoughts Service Backend

## Initial Description
This is for building the backend microservice that handles CRUD operations for positive thoughts. It's part of a larger demo/workshop application showcasing Red Hat OpenShift, Quarkus, and microservices architecture.

## Requirements Discussion

### First Round Questions

**Q1:** I assume the Thought entity will have fields like `id` (UUID or Long), `content` (String), `createdAt`, and `updatedAt` timestamps. Should we also include fields for tracking ratings (like `thumbsUpCount` and `thumbsDownCount` integers) directly on the Thought entity, or will ratings be tracked in a separate table?

**Answer:** Use UUID for identity and include rating fields: `int thumbsUp` and `int thumbsDown` directly on the Thought entity.

**Q2:** For the REST API endpoints, I'm thinking we'll need the standard CRUD operations: `POST /thoughts` (create), `GET /thoughts/{id}` (read one), `GET /thoughts` (list all with pagination), `PUT /thoughts/{id}` (update), and `DELETE /thoughts/{id}` (delete). Should we also include a `GET /thoughts/random` endpoint for the random thought display feature mentioned in the roadmap?

**Answer:** Yes, include the `/random` endpoint. Also include `/thumbsup/{id}` and `/thumbsdown/{id}` endpoints that increment the thumbsUp and thumbsDown values respectively.

**Q3:** I assume we'll use Hibernate Panache with the Active Record pattern for database persistence. Should we use `PanacheEntity` (with auto-generated Long id) or `PanacheEntityBase` (with custom id type like UUID)?

**Answer:** Use `PanacheEntityBase` with UUIDs with these annotations:
```java
@GeneratedValue(generator = "UUID")
@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
```

**Q4:** For Kafka event publishing, I'm thinking we should publish events when thoughts are created, updated, or deleted. Should these events include the full Thought object, or just minimal data like the thought ID and operation type? Also, what should we name the Kafka topic - something like `thoughts.events` or `positive-thoughts.crud`?

**Answer:** Yes, publish the complete Thought in JSON format to a `thoughts.events` topic.

**Q5:** Should the service include input validation for thought content? I'm assuming we'd want minimum and maximum length constraints (perhaps 10-500 characters?), and to reject empty or whitespace-only content.

**Answer:** Yes. Use reasonable defaults for validation constraints.

**Q6:** For database configuration, should we use environment variables and ConfigMaps for database connection details (host, port, database name, credentials), following the twelve-factor app methodology for cloud-native deployments?

**Answer:** Yes, use environment variables and ConfigMaps for database connection details.

**Q7:** I assume we'll need OpenAPI/Swagger documentation auto-generated via the Quarkus OpenAPI extension, and SmallRye Health endpoints for OpenShift liveness and readiness probes. Should we also expose metrics via Micrometer for monitoring?

**Answer:** Yes, include OpenAPI/Swagger documentation, SmallRye Health endpoints, and metrics via Micrometer.

**Q8:** Are there any features explicitly OUT OF SCOPE for this initial backend service? For example, should we exclude user authentication/authorization, thought categories/tags, or search/filtering capabilities in this first iteration?

**Answer:** The following are out of scope for this initial backend service, but should be added to the roadmap for future iterations:
- User authentication/authorization
- Thought categories/tags
- Search/filtering capabilities

### Existing Code to Reference

**Similar Features Identified:**
- Project: `msa-ai-backend` - Use as reference for similar patterns
- Components to potentially reuse: Quarkus microservice patterns, Panache entity patterns, REST endpoint structures, Kafka event publishing implementations

### Follow-up Questions
No follow-up questions needed.

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
N/A - No visual files found in visuals folder.

## Requirements Summary

### Functional Requirements

**Thought Entity:**
- UUID-based identity with specific Hibernate annotations (`@GeneratedValue(generator = "UUID")` and `@GenericGenerator`)
- Content field (String) for storing the positive thought text
- Rating fields: `int thumbsUp` and `int thumbsDown` for tracking user reactions
- Timestamp fields: `createdAt` and `updatedAt` for audit tracking
- Use Hibernate Panache with `PanacheEntityBase` pattern

**REST API Endpoints:**
- `POST /thoughts` - Create a new thought
- `GET /thoughts/{id}` - Retrieve a specific thought by UUID
- `GET /thoughts` - List all thoughts with pagination support
- `PUT /thoughts/{id}` - Update an existing thought
- `DELETE /thoughts/{id}` - Delete a thought
- `GET /thoughts/random` - Retrieve a random thought for display
- `POST /thoughts/thumbsup/{id}` - Increment thumbsUp count for a thought
- `POST /thoughts/thumbsdown/{id}` - Increment thumbsDown count for a thought

**Input Validation:**
- Thought content must not be empty or whitespace-only
- Apply reasonable length constraints on content (e.g., minimum 10 characters, maximum 500 characters)
- Use Hibernate Validator via Quarkus validation extension

**Kafka Event Publishing:**
- Publish events to `thoughts.events` topic
- Events triggered on: create, update, delete operations
- Event payload: Complete Thought object serialized to JSON
- Use Quarkus Kafka extensions (SmallRye Reactive Messaging)

**Database & Persistence:**
- PostgreSQL database running in OpenShift
- Hibernate with Panache for ORM
- Database configuration via environment variables and ConfigMaps
- Follow twelve-factor app methodology for cloud-native configuration

**Observability & Documentation:**
- OpenAPI/Swagger documentation auto-generated via Quarkus OpenAPI extension
- SmallRye Health endpoints for liveness and readiness probes
- Micrometer metrics for monitoring and observability
- JSON-formatted logging for OpenShift log aggregation

### Reusability Opportunities
- Reference `msa-ai-backend` project for:
  - Quarkus microservice structure and patterns
  - Panache entity implementation with UUID handling
  - REST endpoint design and validation patterns
  - Kafka event publishing setup and configuration
  - OpenShift deployment configurations
  - Database migration scripts and schema patterns

### Scope Boundaries

**In Scope:**
- CRUD operations for thoughts (create, read, update, delete)
- Rating functionality (thumbs up/down increments)
- Random thought retrieval endpoint
- Kafka event publishing for thought lifecycle events
- PostgreSQL persistence with Panache ORM
- REST API with OpenAPI documentation
- Health checks and metrics endpoints
- Database configuration via environment variables
- OpenShift deployment readiness

**Out of Scope:**
- User authentication and authorization
- Thought categories or tagging system
- Search and filtering capabilities
- Advanced querying beyond basic CRUD and random selection

**Future Enhancements (Roadmap Items):**
- User authentication/authorization integration
- Thought categories/tags feature
- Search/filtering capabilities for thoughts

### Technical Considerations

**Technology Stack:**
- Quarkus framework for microservice development
- PostgreSQL for database storage
- Apache Kafka (via Red Hat AMQ Streams Operator) for event streaming
- Red Hat OpenShift for container orchestration and deployment
- Maven for build and dependency management
- Hibernate Panache for persistence layer
- SmallRye Reactive Messaging for Kafka integration

**Deployment & Infrastructure:**
- OpenShift deployment with Kubernetes manifests
- ConfigMaps and Secrets for configuration management
- Container image built via Quarkus container image extension
- OpenShift-optimized container images

**Testing Requirements:**
- JUnit 5 with Quarkus test framework
- REST Assured for REST endpoint testing
- Testcontainers for integration testing with PostgreSQL and Kafka

**Integration Points:**
- Kafka topic: `thoughts.events` for publishing thought lifecycle events
- PostgreSQL database for persistent storage
- OpenShift platform for deployment and orchestration
- Potential integration with other microservices consuming thought events

**Existing System Constraints:**
- Must follow patterns established in `msa-ai-backend` project
- Must be deployable on Red Hat OpenShift
- Must use Red Hat AMQ Streams Operator for Kafka
- Must conform to Quarkus best practices and extensions
