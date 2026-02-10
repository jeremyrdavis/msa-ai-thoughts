# Spec Requirements: AI Evaluation Service

## Initial Description
Item #10 from the product roadmap: Create Quarkus microservice that consumes thought events from Kafka, calls the LLM via OpenShift AI to evaluate thought quality, and stores evaluation results.

## Requirements Discussion

### First Round Questions

**Q1:** I assume the AI Evaluation Service should consume thought events from Kafka whenever thoughts are created or updated (not rated), evaluate them using the LLM, and then publish evaluation results back to a Kafka topic. Is that correct, or should it also evaluate on rating changes?

**Answer:** The evaluation service should evaluate newly created thoughts as they are initially published.

**Q2:** For the LLM evaluation, I'm thinking the service should send the thought text to OpenShift AI's LLM endpoint and ask it to evaluate the thought's quality, positivity, coherence, or helpfulness. Should the evaluation include a numeric score (1-10 or 1-5), a text summary, or both? What specific criteria should the LLM evaluate?

**Answer:** The LLM should create a vector of the thought and then evaluate the vector against a predefined list of positive and negative vectors to create a rating.

**Q3:** I assume evaluation results should be stored in PostgreSQL alongside the thought record (either in a new evaluations table or as additional columns in the thoughts table). Which approach do you prefer, or should we use a separate microservice database?

**Answer:** Use the same PostgreSQL database. Can also use it to store the vectors.

**Q4:** For the Quarkus service structure, I'm assuming we'll need a Kafka consumer to listen for thought events, a REST client to call OpenShift AI's LLM endpoint, and a repository layer to persist evaluation results. Should this service also expose REST endpoints to retrieve evaluation results, or will other services only access evaluations through Kafka events?

**Answer:** The Quarkus service that handles ratings will also call the LLM with the Langchain4j extension documented at https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html. The Quarkus evaluation service should store the evaluation in the database and provide a web UI displaying ratings.

**Q5:** I'm thinking the service should handle LLM failures gracefully (timeouts, errors, rate limits) by logging the failure and optionally retrying or marking the evaluation as failed. Should we implement a retry mechanism, and if so, how many retries and what backoff strategy?

**Answer:** Implement 2 retries and then fail.

**Q6:** For the LLM prompt engineering, I assume we'll need to craft a specific prompt that asks the LLM to evaluate the positive thought. Should this prompt be configurable via environment variables or ConfigMap, or hardcoded in the service?

**Answer:** Hardcoded in the service.

**Q7:** I'm thinking the evaluation results event published to Kafka should include the thought ID, evaluation score, evaluation summary text, timestamp, and LLM model used. Is there any other metadata we should include in the event?

**Answer:** Results should simply be stored in the database for now. If a thought's vector is too close to the existing negative vectors it should be set to not display.

**Q8:** Are there any features or behaviors you explicitly want to exclude from this service? For example, batch processing of old thoughts, re-evaluation of previously evaluated thoughts, or user-triggered manual re-evaluations?

**Answer:** Exclude batch processing of old thoughts, re-evaluation of previously evaluated thoughts, and user-triggered manual re-evaluations.

### Existing Code to Reference

No similar existing features identified for reference. However, this service will integrate with:
- Existing Kafka infrastructure from the Event-Driven Architecture implementation (item #8)
- Existing PostgreSQL database from the Thoughts Service Backend (item #2)
- Existing thought domain model that will need enhancement

### Follow-up Questions

**Follow-up 1:** You mentioned the LLM should create a vector of the thought and compare it against predefined positive and negative vectors. Should the Langchain4j extension handle the vector creation automatically, or do we need to configure an embedding model? Also, where should the predefined positive and negative vectors come from - hardcoded in the service, loaded from configuration, or stored in the database?

**Answer:** Preconfigure an embedding model. The predefined vectors should be stored in the database. We will need to create them.

**Follow-up 2:** You mentioned "The Quarkus service that handles ratings will also call the LLM." Just to clarify - should the AI evaluation service be a separate microservice that listens to Kafka thought-created events, or should we extend the existing ratings service to also perform AI evaluations? If it's the latter, should it evaluate when thoughts are created or when they're rated?

**Answer:** Keep the services separate. One service will handle ratings and another will call the LLM. The AI evaluation service should call the LLM when thoughts are published only and mark them APPROVED or REJECTED. An Enum status should be added to the domain model for thoughts.

**Follow-up 3:** You mentioned the evaluation service should "provide a web UI displaying ratings." Should this be a new UI page/component that shows all thoughts with their AI evaluation scores, or should it enhance the existing thought display to show the AI evaluation alongside the thought? Should this UI be part of the existing Next.js frontend or a separate frontend served by the evaluation service?

**Answer:** A separate display served by the evaluation service is fine.

**Follow-up 4:** You said thoughts too close to negative vectors should be "set to not display." Should this mean the thought is marked as hidden in the database but still stored, the thought is completely rejected during creation, or the thought is stored but filtered out of API responses? Should users receive feedback if their thought is filtered as negative?

**Answer:** Marked hidden and stored but not displayed.

**Follow-up 5:** What threshold or similarity score should determine if a thought is "too close" to negative vectors? Should this be configurable or hardcoded?

**Answer:** Make it configurable from a ConfigMap. Please suggest a value in the requirements.

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
No visual assets provided.

## Requirements Summary

### Functional Requirements

**Core Functionality:**
- Create a separate Quarkus microservice dedicated to AI-based thought evaluation
- Consume thought-created events from Kafka topics
- Generate vector embeddings for thought text using a preconfigured embedding model via Langchain4j
- Compare thought vectors against predefined positive and negative vector sets stored in PostgreSQL
- Determine APPROVED or REJECTED status based on vector similarity
- Store evaluation results in the same PostgreSQL database used by other services
- Mark thoughts as hidden (not displayed) if they are too similar to negative vectors
- Provide a web UI (separate from main Next.js frontend) for displaying evaluation results
- Handle LLM failures with retry logic (2 retries before failing)

**Vector Management:**
- Store predefined positive vectors in the database
- Store predefined negative vectors in the database
- These vectors will need to be created and seeded as part of implementation

**Status Tracking:**
- Add an Enum status field to the thought domain model with values: APPROVED, REJECTED
- Thoughts marked REJECTED should be stored but hidden from display

**Configuration:**
- Vector similarity threshold should be configurable via OpenShift ConfigMap
- Suggested threshold value: 0.85 (cosine similarity, where 1.0 is identical and 0.0 is completely different). Values above 0.85 similarity to negative vectors would mark a thought as REJECTED.
- LLM prompts for evaluation should be hardcoded in the service

**Error Handling:**
- Implement retry mechanism: 2 retries on LLM failures
- After retries exhausted, fail the evaluation and log the error
- Failed evaluations should be logged but not block thought storage

**Integration Points:**
- Kafka consumer for thought-created events
- Langchain4j extension for LLM integration (https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html)
- PostgreSQL database (shared with existing services)
- Embedding model configuration for vector generation
- ConfigMap for similarity threshold configuration

**Web UI Requirements:**
- Separate web interface served by the evaluation service
- Display thoughts with their evaluation status
- Show evaluation results and ratings
- This UI is independent of the main Next.js frontend application

### Reusability Opportunities

While no specific similar features were identified, the implementation should follow patterns from:
- Kafka event consumers from the Event-Driven Architecture
- Database repositories and entity mapping from the Thoughts Service Backend
- Quarkus service structure conventions from existing microservices

### Scope Boundaries

**In Scope:**
- New Quarkus microservice for AI evaluation
- Kafka consumer for thought-created events
- Langchain4j integration for embedding model
- Vector similarity comparison logic
- Database schema for storing vectors and evaluation results
- Enhancement to thought domain model (status enum)
- Retry logic for LLM failures
- Separate web UI for displaying evaluation results
- ConfigMap-based similarity threshold configuration
- Initial creation of positive and negative vector sets

**Out of Scope:**
- Batch processing of existing/old thoughts
- Re-evaluation of previously evaluated thoughts
- User-triggered manual re-evaluations
- Publishing evaluation results to Kafka (results only stored in database)
- Integration with existing Next.js frontend (separate UI instead)
- Real-time updates to thought status in existing frontend

**Future Enhancements:**
- User feedback mechanism for improving vector sets
- Dynamic learning from rating patterns
- Admin interface for managing positive/negative vector sets
- Batch re-evaluation capabilities

### Technical Considerations

**Technology Stack:**
- Quarkus framework (consistent with existing services)
- Langchain4j extension for LLM integration
- PostgreSQL with Hibernate Panache
- Kafka with SmallRye Reactive Messaging
- OpenShift AI for embedding model serving
- Maven for build management

**Database Design:**
- Shared PostgreSQL database with existing services
- New tables for storing positive vectors
- New tables for storing negative vectors
- Enhancement to thoughts table to add status enum field
- Evaluation results may be stored in evaluation table or as part of thought record

**Integration Constraints:**
- Must integrate with existing Kafka infrastructure (AMQ Streams)
- Must use same PostgreSQL database as Thoughts Service
- Must follow Quarkus conventions from existing services
- Embedding model must be configured and available in OpenShift AI

**Configuration Management:**
- Similarity threshold in ConfigMap (suggested: 0.85)
- Embedding model endpoint configuration
- LLM prompts hardcoded in service code
- Kafka topic names for consuming thought-created events

**Testing Requirements:**
- Unit tests for vector comparison logic
- Integration tests with Testcontainers for PostgreSQL and Kafka
- REST Assured tests for any exposed endpoints
- Tests for retry mechanism and error handling
- Tests for status enum transitions

**Deployment:**
- Kubernetes/OpenShift deployment manifests
- ConfigMap for similarity threshold
- Service and Route definitions
- Resource limits and health checks
- Integration with OpenShift AI endpoints
