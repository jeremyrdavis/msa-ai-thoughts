# Specification: AI Evaluation Service

## Goal
Create a Quarkus microservice that consumes thought-created events from Kafka, generates vector embeddings using Langchain4j and OpenShift AI, compares them against predefined positive and negative vectors to determine quality, stores evaluation results in PostgreSQL, and provides a web UI for displaying evaluation results.

## User Stories
- As a system administrator, I want thoughts to be automatically evaluated for quality using AI so that inappropriate or negative content can be filtered without manual review
- As a user, I want my positive thoughts to be approved and displayed while negative thoughts are hidden so that the platform maintains a constructive environment

## Specific Requirements

**Kafka Consumer for Thought Events**
- Create Kafka consumer using SmallRye Reactive Messaging listening to thoughts-events topic
- Consume only thought-created events (not updated or deleted)
- Extract thought content and ID from the event payload
- Handle consumer failures gracefully with logging and retry logic
- Use ApplicationScoped service pattern matching ThoughtEventService from existing codebase

**Langchain4j Integration for Embeddings**
- Add Quarkus Langchain4j extension dependency to pom.xml
- Configure embedding model endpoint pointing to OpenShift AI service
- Generate vector embeddings for thought text content using the configured model
- Implement 2 retries with exponential backoff on LLM failures before marking evaluation as failed
- Log all LLM interactions with correlation IDs for troubleshooting

**Vector Similarity Comparison Logic**
- Store predefined positive and negative vector sets in PostgreSQL database tables
- Calculate cosine similarity between thought vector and all negative vectors
- If similarity to any negative vector exceeds configurable threshold (default 0.85), mark thought as REJECTED
- Otherwise mark thought as APPROVED
- Use efficient vector comparison algorithm to minimize computation time
- Threshold value should be loaded from OpenShift ConfigMap at startup

**Database Schema for Vectors and Evaluations**
- Create evaluation_vectors table with columns: id (UUID), vector_data (array or JSON), vector_type (positive/negative enum), label (varchar), created_at
- Create thought_evaluations table with columns: id (UUID), thought_id (FK to thoughts), status (APPROVED/REJECTED enum), similarity_score (decimal), evaluated_at, metadata (JSON)
- Add indexes on thought_id for fast lookups
- Support PostgreSQL array or pgvector extension for efficient vector storage and querying
- Follow Hibernate Panache pattern from existing Thought entity

**Thought Status Enhancement**
- The ThoughtStatus enum already exists with APPROVED, REMOVED, IN_REVIEW values
- Update thought records to set status to APPROVED or REJECTED based on evaluation
- Map REJECTED status to hidden thoughts that should not display in UI
- Ensure status updates are transactional with evaluation result persistence
- Use existing Thought entity pattern with PrePersist and PreUpdate hooks

**REST Endpoints for Evaluation Retrieval**
- Create /evaluations endpoint to list all evaluations with pagination
- Create /evaluations/thought/{thoughtId} endpoint to retrieve specific thought evaluation
- Create /evaluations/stats endpoint to show summary statistics (total evaluated, approved count, rejected count)
- Follow REST conventions from existing ThoughtResource pattern
- Return appropriate HTTP status codes (200, 404, 500)
- Include validation using Jakarta Bean Validation

**Web UI for Evaluation Display**
- Provide simple HTML/JavaScript UI served by Quarkus application
- Display table of evaluated thoughts showing thought content, status, similarity score, and timestamp
- Include filtering by status (APPROVED/REJECTED) and sorting by evaluation date
- Show aggregate statistics dashboard with approval/rejection rates
- Use Qute templating engine for server-side rendering
- Keep UI separate from main Next.js frontend application

**Error Handling and Retry Logic**
- Implement retry mechanism with 2 attempts using SmallRye Fault Tolerance annotations
- Log failures with correlation IDs and thought context for debugging
- On exhausted retries, persist failed evaluation record with error details
- Gracefully handle missing or malformed Kafka events without crashing consumer
- Follow exception mapper pattern from existing ValidationExceptionMapper

**Configuration Management**
- Load similarity threshold from OpenShift ConfigMap using @ConfigProperty
- Suggest default threshold value of 0.85 (cosine similarity scale 0.0-1.0)
- Configure Kafka topic names and bootstrap servers via application.properties
- Configure OpenShift AI embedding model endpoint URL via ConfigMap
- Hardcode evaluation prompts directly in service code as specified in requirements

**Health Checks and Observability**
- Implement Readiness health check for Kafka connection status
- Implement Readiness health check for database connection following DatabaseHealthCheck pattern
- Implement Liveness health check for LLM endpoint availability
- Expose Prometheus metrics for evaluation throughput, success/failure rates, and processing time
- Use SmallRye Health and Micrometer following existing observability patterns

## Visual Design
No visual assets provided.

## Existing Code to Leverage

**Thought Entity and ThoughtStatus Enum**
- Existing Thought entity uses Hibernate Panache extending PanacheEntityBase with UUID primary key
- ThoughtStatus enum already defined with APPROVED, REMOVED, IN_REVIEW values that can be used for evaluation status
- PrePersist and PreUpdate hooks handle timestamp management automatically
- Follow the same entity pattern for new evaluation entities with UUID IDs and audit timestamps

**ThoughtEventService Kafka Producer Pattern**
- Uses SmallRye Reactive Messaging with MutinyEmitter for publishing events
- ApplicationScoped service with injected Channel for event streaming
- Implements separate methods for different event types (created, updated, deleted)
- Follow this pattern for Kafka consumer implementation in reverse direction

**ThoughtResource REST API Pattern**
- JAX-RS resource with Path, Produces, Consumes annotations
- Uses @Transactional for database operations and @Valid for request validation
- Returns Response objects with appropriate HTTP status codes
- Implements pagination using query parameters (page, size)
- Follow this pattern for evaluation endpoints with same conventions

**DatabaseHealthCheck Pattern**
- Implements HealthCheck interface with @Readiness annotation
- ApplicationScoped with injected DataSource dependency
- Uses try-with-resources for connection management and isValid() check
- Return HealthCheckResponse.up() or down() based on connection state
- Replicate for Kafka and LLM health checks

**Maven POM Configuration**
- Quarkus 3.31.2 platform with Java 25 compiler
- Existing dependencies: quarkus-hibernate-orm-panache, quarkus-rest-jackson, quarkus-messaging-kafka, quarkus-jdbc-postgresql
- Add quarkus-langchain4j, quarkus-qute, and quarkus-smallrye-fault-tolerance dependencies
- Follow existing plugin configuration for build and testing with surefire and failsafe

## Out of Scope
- Batch processing or re-evaluation of existing thoughts that were created before this service deployment
- User-triggered manual re-evaluations through UI or API
- Publishing evaluation results to Kafka topics (results only stored in database)
- Real-time WebSocket updates to Next.js frontend showing evaluation status changes
- Admin interface for creating or modifying positive and negative vector sets
- Integration with main Next.js frontend application (separate standalone UI only)
- Machine learning pipeline for dynamically updating vector sets based on user feedback
- A/B testing different similarity thresholds or evaluation algorithms
- Support for multiple languages or internationalization in evaluation logic
- Deletion or modification of historical evaluation records once created
