# Specification: Frontend Application

## Goal
Create two separate Next.js applications: msa-ai-admin for administrative CRUD operations and thought management, and msa-ai-frontend for public-facing random thought display with rating functionality, both integrating with the existing Thoughts Service Backend.

## User Stories
- As an administrator, I want to create, edit, and delete thoughts with status management so that I can curate the collection of positive thoughts displayed to users
- As a user, I want to view random positive thoughts and rate them with thumbs up or down so that I can engage with content and provide feedback

## Specific Requirements

**Backend Entity Enhancement - Thought Status Enum**
- Add status field to existing Thought entity as Enum with three values: APPROVED, REMOVED, IN_REVIEW
- Use Jakarta Persistence @Enumerated annotation with EnumType.STRING for database storage
- Add validation to ensure status is required (nullable = false)
- Include status field in existing REST API responses
- Follow existing Thought entity pattern using Hibernate ORM Panache with @PrePersist and @PreUpdate lifecycle hooks

**Next.js Application Setup - msa-ai-admin**
- Initialize Next.js 14+ project with TypeScript, App Router, and Tailwind CSS
- Configure standalone output mode for Node.js deployment (output: 'standalone' in next.config.js)
- Install shadcn/ui component library with Radix UI primitives
- Set up API client for communicating with existing Quarkus backend at /thoughts endpoints
- Configure environment variables for backend API base URL
- Follow utilitarian design approach with functional, clean UI focused on data management

**Next.js Application Setup - msa-ai-frontend**
- Initialize separate Next.js 14+ project with TypeScript, App Router, and Tailwind CSS
- Configure standalone output mode for Node.js deployment (output: 'standalone' in next.config.js)
- Install shadcn/ui component library with Radix UI primitives
- Set up API client for communicating with existing Quarkus backend at /thoughts endpoints
- Configure environment variables for backend API base URL
- Follow polished, user-friendly design approach with engaging UI focused on user experience

**Admin UI - Thoughts List and Table View**
- Display all thoughts in a table/list view with pagination support (use existing GET /thoughts endpoint with page and size query parameters)
- Show columns: thought content (truncated if necessary), thumbs up count, thumbs down count, rating percentage, status badge
- Calculate and display higher rating percentage (thumbs up vs thumbs down) with visual indicator showing which is dominant
- Include edit and delete action buttons for each thought row
- Use shadcn/ui Table component with sorting and pagination controls
- Implement client-side state management for table data using React hooks

**Admin UI - Create Thought Form**
- Provide dedicated form for creating new thoughts with content textarea field
- Validate content is between 10 and 500 characters (matching backend validation from Thought entity)
- Set default status to IN_REVIEW for newly created thoughts
- Use shadcn/ui Form components with react-hook-form integration
- Display validation errors inline with clear messaging
- Call POST /thoughts endpoint and refresh list on successful creation

**Admin UI - Edit Thought Form**
- Provide edit interface accessible from table view row actions
- Display editable content textarea with existing thought content
- Include status dropdown with three options: APPROVED, REMOVED, IN_REVIEW
- Validate content length between 10 and 500 characters
- Use shadcn/ui Form components with react-hook-form integration
- Call PUT /thoughts/{id} endpoint with updated content and status
- Show current rating statistics as read-only data in edit view

**Admin UI - Delete Thought Functionality**
- Implement delete action from table view row actions
- Show confirmation dialog using shadcn/ui AlertDialog component before deletion
- Call DELETE /thoughts/{id} endpoint on confirmation
- Remove thought from local state and refresh list after successful deletion
- Display error message if deletion fails

**User Frontend - Random Thought Display**
- Fetch and display single random thought using GET /thoughts/random endpoint
- Show thought content prominently in center of page with large, readable typography
- Include thumbs up and thumbs down button/icons using shadcn/ui Button component
- Position rating buttons below thought content with clear visual distinction
- Use single-page application pattern with no navigation on content changes

**User Frontend - Rating System with Visual Feedback**
- Implement thumbs up button that calls POST /thoughts/thumbsup/{id} endpoint
- Implement thumbs down button that calls POST /thoughts/thumbsdown/{id} endpoint
- On rating selection, change selected thumb to solid color and grey out the other thumb
- Disable both buttons after user makes a selection to prevent duplicate ratings
- Use state management to track current rating selection and update UI accordingly
- Display visual feedback immediately on button click before API response returns

**User Frontend - View Another Thought**
- Provide "View Another Thought" button below rating buttons
- On click, fetch new random thought from GET /thoughts/random endpoint
- Replace current thought content and reset rating button states to default
- Update page without navigation or route change (in-place content replacement)
- Show loading state during fetch operation using shadcn/ui skeleton or spinner

**API Integration and Error Handling**
- Create reusable API client service using fetch or axios for both applications
- Handle network errors, 404 responses, and validation errors with user-friendly messages
- Implement loading states during API calls using React state hooks
- Use shadcn/ui Toast component for success and error notifications
- Follow RESTful patterns matching existing ThoughtResource endpoints: GET /thoughts, POST /thoughts, GET /thoughts/{id}, PUT /thoughts/{id}, DELETE /thoughts/{id}, GET /thoughts/random, POST /thoughts/thumbsup/{id}, POST /thoughts/thumbsdown/{id}

## Visual Design

No visual assets provided in planning/visuals folder.

## Existing Code to Leverage

**Thought Entity Model (Thought.java)**
- Extend existing Thought entity with new status Enum field following same pattern as thumbsUp and thumbsDown integer fields
- Reuse existing validation annotations (@NotBlank, @Size) pattern for consistency
- Leverage existing @PrePersist and @PreUpdate lifecycle hooks for automatic timestamp management
- Use existing Panache pattern with public fields and entity methods
- Maintain existing UUID primary key and LocalDateTime timestamp fields

**ThoughtResource REST Endpoints (ThoughtResource.java)**
- Use existing GET /thoughts endpoint with pagination (page and size query params) for admin list view
- Use existing POST /thoughts endpoint for creating new thoughts in admin form
- Use existing PUT /thoughts/{id} endpoint for updating thoughts including new status field
- Use existing DELETE /thoughts/{id} endpoint for delete functionality in admin
- Use existing GET /thoughts/random endpoint for fetching random thoughts in user frontend
- Use existing POST /thoughts/thumbsup/{id} and POST /thoughts/thumbsdown/{id} endpoints for rating functionality

**Quarkus Backend Technology Stack (pom.xml)**
- Frontend applications will integrate with existing Quarkus backend using JAX-RS REST endpoints
- Expect JSON responses with Jackson serialization matching existing pattern
- Backend includes validation with Hibernate Validator, so frontend should handle 400 Bad Request responses
- Backend uses UUID for IDs, so frontend should handle UUID string format in API calls
- Backend includes Kafka messaging via ThoughtEventService, which will automatically publish events for create, update, and delete operations

**Database Schema Pattern (Thought.java)**
- New status field should follow existing column naming convention using snake_case (@Column name = "status")
- Status Enum should be stored as VARCHAR/TEXT in PostgreSQL using EnumType.STRING
- New field requires database migration to add status column with NOT NULL constraint and default value
- Leverage existing thumbs_up and thumbs_down columns for rating statistics calculation in admin UI

**Rating System Logic (ThoughtResource.java)**
- Reuse existing thumbsUp and thumbsDown counter increment pattern in backend endpoints
- Frontend should call POST endpoints and display returned Thought object with updated counts
- Admin UI can calculate percentage by dividing individual count by total (thumbsUp + thumbsDown)
- Handle edge case where total votes is zero to avoid division by zero errors

## Out of Scope
- Authentication and authorization implementation (both UIs remain open access)
- User session management and login flows
- Deployment configurations for OpenShift or containerization
- CI/CD pipeline setup for frontend applications
- Specific AI evaluation features or AI integration in frontend
- Admin-specific backend endpoints (existing endpoints are sufficient)
- Read-only list view of all thoughts in user-facing frontend (random thoughts only)
- Status field automatic updates based on criteria (status is manually editable only)
- Real-time updates or WebSocket integration for live data
- Analytics dashboard or reporting features beyond basic rating statistics
