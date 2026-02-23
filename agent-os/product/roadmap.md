# Product Roadmap

1. [ ] **PostgreSQL Database Setup** — Configure PostgreSQL database in OpenShift with proper schema for storing positive thoughts, including tables for thoughts and ratings with appropriate indexes and constraints. `S`

2. [x] **Thoughts Service Backend** — Create Quarkus microservice with REST endpoints for CRUD operations on positive thoughts, connecting to PostgreSQL database with proper entity mapping and transaction management. `M`

3. [x] **Frontend Application** — Build Next.js application with shadcn/ui components for displaying thoughts, including forms for creating/editing thoughts and responsive layout, compiled to static site. `M`

4. [ ] **Frontend Service Integration** — Create Quarkus microservice to serve the static Next.js frontend and provide backend API endpoints, implementing proper CORS and routing configuration. `S`

5. [x] **Random Thought Display** — Implement frontend and backend logic to fetch and display random positive thoughts from the database, with smooth UI transitions and error handling. `S`

6. [x] **Rating System** — Add thumbs up/down rating functionality with backend persistence to PostgreSQL, updating rating counts in real-time and preventing duplicate votes. `M`

7. [ ] **Kafka Infrastructure** — Deploy Red Hat AMQ Streams Operator and configure Kafka cluster in OpenShift with topics for thought events and rating events. `S`

8. [x] **Event-Driven Architecture** — Refactor services to publish events to Kafka when thoughts are created/updated/rated, implementing event consumers to decouple service communication. `M`

9. [ ] **OpenShift AI LLM Setup** — Configure Red Hat OpenShift AI to serve an LLM model with appropriate resource allocation and endpoint configuration for thought evaluation. `M`

10. [x] **AI Evaluation Service** — Create Quarkus microservice that consumes thought events from Kafka, calls the LLM via OpenShift AI to evaluate thought quality, and stores evaluation results. `L`

11. [ ] **AI Evaluation UI** — Extend frontend to display AI evaluation results for thoughts, showing LLM analysis and recommendations with clear visual indicators. `S`

12. [ ] **Developer Hub Integration** — Register all microservices in Red Hat Developer Hub with proper metadata, API documentation, and service relationships for service catalog discovery. `M`

13. [x] **OpenShift Deployment Manifests** — Create complete Kubernetes/OpenShift deployment configurations including Services, Routes, ConfigMaps, and resource limits for all microservices. `M`

14. [ ] **Workshop Materials** — Develop progressive workshop guide with checkpoints starting from basic microservices through AI integration, including setup instructions and validation steps. `L`

> Notes
> - Order items by technical dependencies and product architecture
> - Each item should represent an end-to-end (frontend + backend) functional and testable feature
