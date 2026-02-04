# Tech Stack

## Container Platform & Runtime
- **Container Platform:** Red Hat OpenShift (Kubernetes-based container orchestration)
- **Application Framework:** Quarkus (supersonic subatomic Java framework optimized for containers and cloud)
- **Language/Runtime:** Java (via Quarkus native and JVM modes)
- **Package Manager:** Maven (for Quarkus microservices)

## Frontend
- **JavaScript Framework:** Next.js (React-based framework, compiled to static site)
- **UI Components:** shadcn/ui (accessible component library built on Radix UI)
- **CSS Framework:** Tailwind CSS (via shadcn/ui)
- **Frontend Serving:** Static site served by Quarkus microservice

## Database & Storage
- **Database:** PostgreSQL (running in OpenShift)
- **ORM/Query Builder:** Hibernate with Panache (Quarkus persistence layer)
- **Connection Pooling:** Agroal (included with Quarkus)

## Messaging & Events
- **Message Broker:** Apache Kafka (deployed via Red Hat AMQ Streams Operator)
- **Event Streaming:** Kafka topics for thought events and rating events
- **Kafka Integration:** Quarkus Kafka extensions (SmallRye Reactive Messaging)

## AI & Machine Learning
- **AI Platform:** Red Hat OpenShift AI (for serving LLMs)
- **LLM Integration:** REST API calls from Quarkus services to OpenShift AI endpoints
- **AI Use Case:** Thought evaluation and analysis

## Developer Experience
- **Developer Portal:** Red Hat Developer Hub (based on Backstage)
- **Service Catalog:** Developer Hub service registry for microservices discovery
- **API Documentation:** OpenAPI/Swagger (via Quarkus OpenAPI extension)

## Testing & Quality
- **Backend Testing:** JUnit 5 (with Quarkus test framework)
- **REST Testing:** REST Assured (included with Quarkus)
- **Test Containers:** Testcontainers for integration testing with PostgreSQL and Kafka

## Deployment & Infrastructure
- **Container Orchestration:** Red Hat OpenShift
- **Deployment Strategy:** Kubernetes manifests (Deployments, Services, Routes)
- **Configuration Management:** ConfigMaps and Secrets in OpenShift
- **Operators:** Red Hat AMQ Streams Operator (for Kafka), OpenShift AI Operator

## Build & CI/CD
- **Build Tool:** Maven (for Quarkus services)
- **Container Images:** Quarkus container image extension with OpenShift optimizations
- **CI/CD:** OpenShift Pipelines (Tekton) or GitHub Actions (depending on workshop setup)

## Monitoring & Observability
- **Metrics:** Micrometer (via Quarkus Micrometer extension)
- **Health Checks:** Quarkus SmallRye Health (liveness and readiness probes)
- **Logging:** Quarkus logging with JSON formatting for OpenShift log aggregation
- **Tracing:** OpenTelemetry support (via Quarkus extensions)

## Additional Services & Tools
- **REST Client:** Quarkus REST Client for inter-service communication
- **Serialization:** Jackson (JSON processing)
- **Validation:** Hibernate Validator (via Quarkus validation extension)
