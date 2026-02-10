# Task Breakdown: AI Evaluation Service

## Overview
Total Tasks: 48 organized into 6 task groups
Target Platform: Quarkus microservice with Langchain4j, PostgreSQL, and Kafka integration

## Task List

### Project Setup and Dependencies

#### Task Group 1: Maven Configuration and Initial Project Structure
**Dependencies:** None

- [x] 1.0 Complete project setup and dependencies
  - [x] 1.1 Add Quarkus Langchain4j extension to pom.xml
    - Add quarkus-langchain4j dependency
    - Add quarkus-langchain4j-openai or appropriate embedding model dependency
    - Verify compatibility with Quarkus 3.31.2
  - [x] 1.2 Add Quarkus Qute templating dependency
    - Add quarkus-qute dependency for web UI templating
  - [x] 1.3 Add SmallRye Fault Tolerance dependency
    - Add quarkus-smallrye-fault-tolerance for retry logic
  - [x] 1.4 Add Micrometer and metrics dependencies
    - Add quarkus-micrometer-registry-prometheus for observability
  - [x] 1.5 Verify existing dependencies are present
    - Confirm quarkus-hibernate-orm-panache
    - Confirm quarkus-messaging-kafka
    - Confirm quarkus-jdbc-postgresql
    - Confirm quarkus-rest-jackson
    - Confirm quarkus-smallrye-health
  - [x] 1.6 Configure application.properties for base settings
    - Set Kafka bootstrap servers configuration placeholder
    - Set database connection properties placeholder
    - Add ConfigProperty references for similarity threshold
    - Add OpenShift AI embedding model endpoint configuration placeholder

**Acceptance Criteria:**
- All required dependencies added to pom.xml
- Maven build completes successfully
- No dependency conflicts
- application.properties contains all configuration placeholders

### Database Layer

#### Task Group 2: Database Schema and Entity Models
**Dependencies:** Task Group 1

- [x] 2.0 Complete database layer
  - [x] 2.1 Write 2-8 focused tests for entity models
    - Limit to 2-8 highly focused tests maximum
    - Test EvaluationVector entity creation and validation
    - Test ThoughtEvaluation entity creation and relationships
    - Test vector data serialization/deserialization
    - Skip exhaustive validation testing of all fields
  - [x] 2.2 Create EvaluationVector entity
    - Extend PanacheEntityBase with UUID primary key
    - Fields: id (UUID), vectorData (array or JSON), vectorType (enum: POSITIVE/NEGATIVE), label (String), createdAt (LocalDateTime)
    - Use @PrePersist annotation for timestamp management
    - Follow pattern from existing Thought entity
    - Add validation annotations (NotNull, Size constraints)
  - [x] 2.3 Create VectorType enum
    - Values: POSITIVE, NEGATIVE
    - Simple enum with no additional logic
  - [x] 2.4 Create ThoughtEvaluation entity
    - Extend PanacheEntityBase with UUID primary key
    - Fields: id (UUID), thoughtId (UUID FK), status (ThoughtStatus enum), similarityScore (BigDecimal), evaluatedAt (LocalDateTime), metadata (JSON)
    - Use @PrePersist for timestamp management
    - Add @ManyToOne relationship to Thought entity if accessible
    - Add validation annotations
  - [x] 2.5 Enhance ThoughtStatus enum
    - Verify existing values: APPROVED, REMOVED, IN_REVIEW
    - Add REJECTED value if not present
    - Ensure APPROVED value exists for positive evaluations
  - [x] 2.6 Create database migration for evaluation_vectors table
    - Columns: id (UUID primary key), vector_data (array or jsonb), vector_type (varchar with check constraint), label (varchar 255), created_at (timestamp)
    - Add index on vector_type for filtering queries
    - Consider pgvector extension if available for efficient vector operations
  - [x] 2.7 Create database migration for thought_evaluations table
    - Columns: id (UUID primary key), thought_id (UUID FK), status (varchar with check constraint), similarity_score (decimal), evaluated_at (timestamp), metadata (jsonb)
    - Add foreign key constraint to thoughts table
    - Add index on thought_id for fast lookups
    - Add index on status for filtering queries
  - [x] 2.8 Create seed data migration for initial vectors
    - Insert at least 3 predefined positive vectors
    - Insert at least 3 predefined negative vectors
    - Use realistic vector dimensions (e.g., 384 or 768 dimensions based on embedding model)
    - Add descriptive labels for each vector
  - [x] 2.9 Ensure database layer tests pass
    - Run ONLY the 2-8 tests written in 2.1
    - Verify entities can be persisted and retrieved
    - Verify migrations run successfully
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 2.1 pass
- All entity models follow Panache pattern with UUID primary keys
- Migrations create tables with appropriate constraints and indexes
- ThoughtStatus enum includes APPROVED and REJECTED values
- Seed data provides initial positive and negative vectors
- Entities use proper validation annotations

### Kafka Consumer and Event Processing

#### Task Group 3: Kafka Integration for Thought Events
**Dependencies:** Task Group 2

- [ ] 3.0 Complete Kafka consumer layer
  - [ ] 3.1 Write 2-8 focused tests for Kafka consumer
    - Limit to 2-8 highly focused tests maximum
    - Test thought-created event consumption
    - Test extraction of thought ID and content from event
    - Test consumer error handling for malformed events
    - Skip testing all event types and edge cases
  - [ ] 3.2 Create ThoughtEvent DTO
    - Fields: eventType (String), thoughtId (UUID), thoughtContent (String), timestamp (LocalDateTime)
    - Add Jackson annotations for JSON deserialization
    - Support for thought-created event type only
  - [ ] 3.3 Create ThoughtEvaluationConsumer service
    - ApplicationScoped service class
    - Use @Incoming annotation with SmallRye Reactive Messaging
    - Listen to thoughts-events topic (or configured topic name)
    - Inject dependencies: EvaluationService, Logger
  - [ ] 3.4 Implement event filtering logic
    - Filter for thought-created events only
    - Ignore updated, deleted, and other event types
    - Extract thought ID and content from event payload
    - Log ignored events at DEBUG level
  - [ ] 3.5 Implement error handling for consumer
    - Wrap processing in try-catch block
    - Log failures with correlation ID and thought context
    - Handle malformed JSON gracefully without crashing consumer
    - Use @Fallback annotation from SmallRye Fault Tolerance if applicable
    - Continue consuming subsequent messages after failures
  - [ ] 3.6 Configure Kafka consumer properties
    - Set consumer group ID in application.properties
    - Configure auto-commit settings
    - Set max poll records and timeout values
    - Add topic name configuration property
  - [ ] 3.7 Ensure Kafka consumer tests pass
    - Run ONLY the 2-8 tests written in 3.1
    - Verify events are consumed and filtered correctly
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 3.1 pass
- Consumer listens to thoughts-events topic
- Only thought-created events are processed
- Consumer handles errors gracefully without crashing
- Malformed events are logged and skipped
- Consumer follows ThoughtEventService producer pattern in reverse

### Langchain4j Integration and Vector Evaluation

#### Task Group 4: AI Embedding and Similarity Evaluation Logic
**Dependencies:** Task Group 3

- [ ] 4.0 Complete AI evaluation logic
  - [ ] 4.1 Write 2-8 focused tests for evaluation service
    - Limit to 2-8 highly focused tests maximum
    - Test vector embedding generation
    - Test similarity calculation between vectors
    - Test APPROVED/REJECTED determination logic
    - Skip testing all edge cases and retry scenarios
  - [ ] 4.2 Configure Langchain4j embedding model
    - Add embedding model configuration in application.properties
    - Set OpenShift AI endpoint URL via @ConfigProperty
    - Configure model name and parameters
    - Set timeout values for LLM calls
  - [ ] 4.3 Create EmbeddingService
    - ApplicationScoped service class
    - Inject Langchain4j EmbeddingModel dependency
    - Method: generateEmbedding(String text) returns float[] or List<Float>
    - Log LLM interactions with correlation IDs
  - [ ] 4.4 Implement retry logic for LLM calls
    - Use @Retry annotation from SmallRye Fault Tolerance
    - Configure 2 retry attempts with exponential backoff
    - Log each retry attempt with context
    - On exhausted retries, throw exception to mark evaluation as failed
  - [ ] 4.5 Create VectorSimilarityService
    - ApplicationScoped service class
    - Method: calculateCosineSimilarity(float[] vectorA, float[] vectorB) returns double
    - Implement efficient cosine similarity algorithm
    - Normalize vectors if needed
    - Add input validation for vector dimensions
  - [ ] 4.6 Create EvaluationService orchestrator
    - ApplicationScoped service class
    - Inject: EmbeddingService, VectorSimilarityService, EvaluationVector repository, ThoughtEvaluation repository
    - Inject similarity threshold via @ConfigProperty(name = "evaluation.similarity.threshold", defaultValue = "0.85")
    - Method: evaluateThought(UUID thoughtId, String thoughtContent) returns ThoughtEvaluation
  - [ ] 4.7 Implement evaluation logic
    - Generate embedding vector for thought content using EmbeddingService
    - Retrieve all NEGATIVE vectors from database
    - Calculate cosine similarity between thought vector and each negative vector
    - If any similarity exceeds threshold, mark as REJECTED
    - Otherwise mark as APPROVED
    - Store highest similarity score in evaluation result
  - [ ] 4.8 Implement evaluation persistence
    - Create ThoughtEvaluation entity with calculated status and similarity score
    - Add metadata JSON with evaluation timestamp and model info
    - Persist evaluation to database within transaction
    - Update corresponding Thought entity status field (APPROVED or REJECTED)
    - Ensure transactional consistency
  - [ ] 4.9 Ensure evaluation service tests pass
    - Run ONLY the 2-8 tests written in 4.1
    - Verify embedding generation works
    - Verify similarity calculation is accurate
    - Verify APPROVED/REJECTED logic works correctly
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 4.1 pass
- Langchain4j embedding model configured and callable
- Retry logic implemented with 2 attempts and exponential backoff
- Cosine similarity calculation is accurate and efficient
- Evaluation correctly determines APPROVED vs REJECTED status
- Threshold value loaded from ConfigMap/application.properties (default 0.85)
- Evaluation results persisted transactionally
- Thought status updated based on evaluation

### REST API Endpoints

#### Task Group 5: Evaluation Retrieval REST API
**Dependencies:** Task Group 4

- [ ] 5.0 Complete REST API layer
  - [ ] 5.1 Write 2-8 focused tests for REST endpoints
    - Limit to 2-8 highly focused tests maximum
    - Test GET /evaluations with pagination
    - Test GET /evaluations/thought/{thoughtId}
    - Test GET /evaluations/stats endpoint
    - Skip testing all query parameters and error scenarios
  - [ ] 5.2 Create EvaluationDTO
    - Fields: id, thoughtId, thoughtContent (optional), status, similarityScore, evaluatedAt
    - Add Jackson annotations for JSON serialization
    - Include builder or constructor for easy creation
  - [ ] 5.3 Create EvaluationStatsDTO
    - Fields: totalEvaluated, approvedCount, rejectedCount, averageSimilarityScore
    - Add Jackson annotations
  - [ ] 5.4 Create EvaluationResource REST controller
    - JAX-RS resource with @Path("/evaluations")
    - @Produces(MediaType.APPLICATION_JSON)
    - Follow pattern from existing ThoughtResource
    - Inject ThoughtEvaluation repository
  - [ ] 5.5 Implement GET /evaluations endpoint
    - Support pagination with query parameters: page (default 0), size (default 20)
    - Return list of EvaluationDTO
    - Use PanacheQuery for pagination
    - Return 200 status with paginated results
  - [ ] 5.6 Implement GET /evaluations/thought/{thoughtId} endpoint
    - Path parameter: thoughtId (UUID)
    - Return single EvaluationDTO for specified thought
    - Return 404 if evaluation not found
    - Return 200 with evaluation if found
    - Add @Valid annotation if using Bean Validation
  - [ ] 5.7 Implement GET /evaluations/stats endpoint
    - Calculate total evaluations count
    - Count APPROVED evaluations
    - Count REJECTED evaluations
    - Calculate average similarity score across all evaluations
    - Return EvaluationStatsDTO
    - Return 200 status
  - [ ] 5.8 Add error handling and exception mappers
    - Handle IllegalArgumentException for invalid UUIDs
    - Return appropriate error response DTOs
    - Follow pattern from existing ValidationExceptionMapper
    - Return consistent error format with message and status
  - [ ] 5.9 Ensure REST API tests pass
    - Run ONLY the 2-8 tests written in 5.1
    - Verify endpoints return correct data and status codes
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 5.1 pass
- All three endpoints functional and return correct data
- Pagination works correctly on /evaluations endpoint
- 404 returned when evaluation not found
- 200 returned with valid data for successful requests
- Stats endpoint calculates accurate counts and averages
- Error responses follow consistent format
- Follows ThoughtResource REST patterns

### Web UI and Observability

#### Task Group 6: Web Interface, Health Checks, and Metrics
**Dependencies:** Task Group 5

- [ ] 6.0 Complete web UI and observability
  - [ ] 6.1 Write 2-8 focused tests for health checks
    - Limit to 2-8 highly focused tests maximum
    - Test database health check returns UP when connected
    - Test Kafka health check returns UP when connected
    - Test LLM endpoint health check
    - Skip testing all failure scenarios
  - [ ] 6.2 Create Qute templates for evaluation UI
    - Create src/main/resources/templates/evaluations.html
    - Table layout with columns: Thought Content, Status, Similarity Score, Evaluated At
    - Add filtering controls for status (APPROVED/REJECTED)
    - Add sorting controls by evaluation date
    - Use simple HTML/CSS styling (no complex frameworks)
    - Include pagination controls
  - [ ] 6.3 Create Qute template for stats dashboard
    - Create src/main/resources/templates/stats.html
    - Display total evaluations count
    - Display approved count and percentage
    - Display rejected count and percentage
    - Display average similarity score
    - Use simple card layout with CSS styling
  - [ ] 6.4 Create EvaluationUIResource controller
    - JAX-RS resource with @Path("/ui/evaluations")
    - @Produces(MediaType.TEXT_HTML)
    - Inject Template instances for evaluations and stats pages
    - GET /ui/evaluations endpoint renders evaluations.html with data
    - GET /ui/evaluations/stats endpoint renders stats.html with data
  - [ ] 6.5 Implement database health check
    - Create DatabaseHealthCheck class
    - Implement HealthCheck interface with @Readiness annotation
    - ApplicationScoped with injected DataSource
    - Use try-with-resources and connection.isValid() check
    - Return HealthCheckResponse.up() or down()
    - Follow existing DatabaseHealthCheck pattern
  - [ ] 6.6 Implement Kafka health check
    - Create KafkaHealthCheck class
    - Implement HealthCheck interface with @Readiness annotation
    - ApplicationScoped with injected Kafka consumer health indicator
    - Check consumer connectivity status
    - Return HealthCheckResponse.up() or down()
  - [ ] 6.7 Implement LLM endpoint health check
    - Create LLMHealthCheck class
    - Implement HealthCheck interface with @Liveness annotation
    - ApplicationScoped with injected EmbeddingService
    - Attempt simple embedding call or endpoint ping
    - Return HealthCheckResponse.up() or down()
    - Add timeout to prevent hanging health checks
  - [ ] 6.8 Add Prometheus metrics
    - Use @Counted annotation on EvaluationService.evaluateThought method for throughput
    - Use @Timed annotation for processing time metrics
    - Create custom metrics for approval/rejection rates using MeterRegistry
    - Add metric for LLM call success/failure rates
    - Add metric for average similarity scores
  - [ ] 6.9 Ensure health checks and UI tests pass
    - Run ONLY the 2-8 tests written in 6.1
    - Verify health endpoints return correct status
    - Verify UI pages render with data
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 6.1 pass
- Web UI displays evaluations in table format with filtering and sorting
- Stats dashboard shows accurate metrics
- Database health check returns readiness status
- Kafka health check returns readiness status
- LLM health check returns liveness status
- Prometheus metrics exposed at /q/metrics
- Metrics track evaluation throughput, success/failure rates, and processing time
- UI is independent from main Next.js frontend

### Final Testing and Integration

#### Task Group 7: Test Review, Gap Analysis, and End-to-End Verification
**Dependencies:** Task Groups 1-6

- [ ] 7.0 Review existing tests and fill critical gaps only
  - [ ] 7.1 Review tests from Task Groups 1-6
    - Review tests from database layer (Task 2.1)
    - Review tests from Kafka consumer (Task 3.1)
    - Review tests from evaluation service (Task 4.1)
    - Review tests from REST API (Task 5.1)
    - Review tests from health checks (Task 6.1)
    - Total existing tests: approximately 10-40 tests
  - [ ] 7.2 Analyze test coverage gaps for THIS feature only
    - Identify critical end-to-end workflows lacking coverage
    - Focus on integration between Kafka consumer, evaluation service, and database persistence
    - Identify gaps in retry logic and error handling flows
    - Do NOT assess entire application test coverage
    - Prioritize end-to-end workflows over unit test gaps
  - [ ] 7.3 Write up to 10 additional strategic tests maximum
    - Add integration test for complete flow: Kafka event to evaluation to database persistence
    - Add test for retry mechanism with LLM failure scenarios
    - Add test for threshold configuration loading from ConfigMap
    - Add test for transaction rollback on evaluation failure
    - Add test for thought status update when evaluation completes
    - Focus on integration points and critical workflows only
    - Do NOT write comprehensive coverage for all scenarios
    - Skip edge cases unless business-critical
  - [ ] 7.4 Run feature-specific tests only
    - Run ONLY tests related to AI Evaluation Service feature
    - Expected total: approximately 20-50 tests maximum
    - Do NOT run the entire application test suite
    - Verify all critical workflows pass
  - [ ] 7.5 Manual end-to-end verification
    - Start local Kafka, PostgreSQL, and OpenShift AI mock or connection
    - Publish test thought-created event to Kafka
    - Verify evaluation service consumes event
    - Verify embedding generated via Langchain4j
    - Verify similarity calculation against negative vectors
    - Verify evaluation result persisted in database
    - Verify thought status updated correctly
    - Verify REST API returns evaluation data
    - Verify UI displays evaluation
    - Verify health checks return UP status
    - Verify Prometheus metrics are exposed
  - [ ] 7.6 Configuration validation
    - Verify similarity threshold configurable via ConfigMap/application.properties
    - Verify OpenShift AI endpoint URL configurable
    - Verify Kafka topic names configurable
    - Test with different threshold values (0.7, 0.85, 0.95)
    - Verify default threshold of 0.85 works when not configured
  - [ ] 7.7 Error scenario verification
    - Test behavior when LLM endpoint is unreachable
    - Verify retry logic executes 2 times
    - Verify evaluation marked as failed after retry exhaustion
    - Test behavior with malformed Kafka events
    - Verify consumer continues processing after error
    - Test database constraint violations are handled gracefully

**Acceptance Criteria:**
- All feature-specific tests pass (approximately 20-50 tests total)
- Critical end-to-end workflows verified manually
- No more than 10 additional tests added when filling in testing gaps
- Complete flow from Kafka event to database persistence works
- Retry logic executes correctly on LLM failures
- Error scenarios handled gracefully without crashing service
- Configuration values load correctly from application.properties/ConfigMap
- Health checks indicate service readiness and liveness
- Prometheus metrics track key evaluation metrics
- Web UI displays evaluations correctly

## Execution Order

Recommended implementation sequence:
1. Project Setup and Dependencies (Task Group 1)
2. Database Layer (Task Group 2)
3. Kafka Consumer and Event Processing (Task Group 3)
4. Langchain4j Integration and Vector Evaluation (Task Group 4)
5. REST API Endpoints (Task Group 5)
6. Web UI and Observability (Task Group 6)
7. Final Testing and Integration (Task Group 7)

## Notes

- This is a new Quarkus microservice separate from existing services
- Service consumes from Kafka but does not publish evaluation results back to Kafka
- Evaluation results stored in shared PostgreSQL database with other services
- Web UI is standalone, not integrated with main Next.js frontend
- Similarity threshold default of 0.85 (cosine similarity) should be configurable
- LLM retry logic: 2 attempts with exponential backoff before marking as failed
- Focus testing on critical workflows; avoid comprehensive edge case coverage during development
