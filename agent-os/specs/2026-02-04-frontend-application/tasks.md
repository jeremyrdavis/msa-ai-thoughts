# Task Breakdown: Frontend Application

## Overview
Total Task Groups: 5
Estimated Total Tasks: ~45 individual tasks

This feature creates two separate Next.js applications (msa-ai-admin and msa-ai-frontend) that integrate with the existing Thoughts Service Backend, along with backend enhancements to support status management.

## Task List

### Backend Enhancement - Thought Entity Status Field

#### Task Group 1: Database Schema and Entity Updates
**Dependencies:** None

- [x] 1.0 Complete backend entity and database changes for status field
  - [x] 1.1 Write 2-8 focused tests for Thought entity status functionality
    - Test status enum persistence (EnumType.STRING storage)
    - Test status field is required (nullable = false validation)
    - Test status field appears in JSON responses
    - Limit to 2-8 highly focused tests maximum
    - Skip exhaustive coverage of all enum values and edge cases
  - [x] 1.2 Add status enum to Thought entity
    - Create ThoughtStatus enum with three values: APPROVED, REMOVED, IN_REVIEW
    - Use Jakarta Persistence @Enumerated annotation with EnumType.STRING
    - Add status field to Thought entity as ThoughtStatus type
    - Apply @Column(name = "status", nullable = false) annotation
    - Follow existing entity pattern using Panache with public fields
    - Maintain consistency with existing thumbsUp and thumbsDown fields
  - [x] 1.3 Set default status for new thoughts
    - Add default status = IN_REVIEW in @PrePersist lifecycle hook
    - Ensure existing @PrePersist pattern is maintained for timestamps
    - Verify status is included in entity validation
  - [x] 1.4 Create database migration for status column
    - Add status column as VARCHAR/TEXT with NOT NULL constraint
    - Set default value to 'IN_REVIEW' for new rows
    - Update existing rows with default 'IN_REVIEW' status
    - Create index on status column for filtering queries
    - Follow migration naming conventions
    - Ensure migration is reversible
  - [x] 1.5 Verify status field in REST API responses
    - Ensure status appears in JSON serialization for all endpoints
    - Test GET /thoughts response includes status
    - Test GET /thoughts/{id} response includes status
    - Test POST /thoughts response includes status
    - Test PUT /thoughts/{id} response includes status
  - [x] 1.6 Ensure backend entity tests pass
    - Run ONLY the 2-8 tests written in 1.1
    - Verify migration runs successfully
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 1.1 pass
- ThoughtStatus enum created with three values
- Status field added to Thought entity with proper annotations
- Database migration successfully adds status column
- Status field appears in all REST API JSON responses
- Default status IN_REVIEW set for new thoughts

### Admin Application - Project Setup and Infrastructure

#### Task Group 2: msa-ai-admin Next.js Application Setup
**Dependencies:** None

- [x] 2.0 Complete msa-ai-admin project initialization
  - [x] 2.1 Initialize Next.js 14+ project with TypeScript
    - Create new Next.js project with TypeScript template
    - Enable App Router (not Pages Router)
    - Configure project name as msa-ai-admin
    - Verify project structure uses /app directory
  - [x] 2.2 Configure Tailwind CSS
    - Install Tailwind CSS with PostCSS and Autoprefixer
    - Configure tailwind.config.js with content paths for /app directory
    - Set up global styles in app/globals.css
    - Verify Tailwind utilities work in test component
  - [x] 2.3 Install and configure shadcn/ui component library
    - Initialize shadcn/ui with Radix UI primitives
    - Install core shadcn/ui components: Button, Table, Form, Input, Textarea, Select, AlertDialog, Toast
    - Configure components directory structure
    - Verify component imports work correctly
  - [x] 2.4 Configure standalone output mode for Node.js deployment
    - Set output: 'standalone' in next.config.js
    - Configure environment variables structure
    - Create .env.local template file
    - Document deployment configuration in README
  - [x] 2.5 Set up API client service for backend communication
    - Create lib/api-client.ts with fetch-based service
    - Configure base URL from environment variable (NEXT_PUBLIC_API_BASE_URL)
    - Implement error handling wrapper for API calls
    - Create TypeScript interfaces for Thought entity matching backend schema
    - Include all REST endpoints: GET /thoughts, POST /thoughts, GET /thoughts/{id}, PUT /thoughts/{id}, DELETE /thoughts/{id}
  - [x] 2.6 Create project documentation
    - Document project structure in README
    - Document environment variables required
    - Document API integration patterns
    - Include local development setup instructions

**Acceptance Criteria:**
- Next.js 14+ project initialized with TypeScript and App Router
- Tailwind CSS configured and functional
- shadcn/ui components installed and accessible
- Standalone output mode configured in next.config.js
- API client service created with all CRUD endpoints
- Environment variables configured for backend URL
- README documentation complete

### Admin Application - CRUD Functionality

#### Task Group 3: Thoughts Management UI (List, Create, Edit, Delete)
**Dependencies:** Task Group 2

- [x] 3.0 Complete admin CRUD functionality
  - [x] 3.1 Write 2-8 focused tests for admin CRUD operations
    - Test thoughts list renders with pagination
    - Test create form submission creates new thought
    - Test edit form updates thought and status
    - Test delete action removes thought after confirmation
    - Limit to 2-8 highly focused tests maximum
    - Skip exhaustive testing of all form states and validation scenarios
  - [x] 3.2 Create thoughts list/table view page
    - Create app/thoughts/page.tsx as main table view route
    - Use shadcn/ui Table component for display
    - Implement columns: thought content (truncated to 100 chars), thumbs up count, thumbs down count, rating percentage, status badge
    - Calculate higher rating percentage: (max(thumbsUp, thumbsDown) / (thumbsUp + thumbsDown)) * 100
    - Display visual indicator showing which rating is dominant (thumbs up or down)
    - Handle edge case where total votes is zero (display "No ratings yet")
    - Add edit and delete action buttons in each row
    - Follow utilitarian design approach: clean, functional, data-focused
  - [x] 3.3 Implement pagination for thoughts list
    - Use existing GET /thoughts endpoint with page and size query parameters
    - Add pagination controls below table using shadcn/ui Button components
    - Track current page in React state
    - Display current page and total pages information
    - Default to 20 thoughts per page
  - [x] 3.4 Implement table sorting functionality
    - Add sortable columns: created timestamp, thumbs up count, thumbs down count
    - Use client-side sorting with React state
    - Display sort indicator in column headers
    - Default sort: newest thoughts first (createdAt descending)
  - [x] 3.5 Create thought creation form
    - Create app/thoughts/new/page.tsx as dedicated creation route
    - Use shadcn/ui Form components with react-hook-form integration
    - Add content textarea field with character counter
    - Validate content length: 10 to 500 characters (matching backend validation)
    - Set default status to IN_REVIEW (hidden from user, set automatically)
    - Display validation errors inline with clear messaging
    - Call POST /thoughts endpoint on submission
    - Navigate back to list view on successful creation
    - Show success toast notification using shadcn/ui Toast
  - [x] 3.6 Create thought edit form
    - Create app/thoughts/[id]/edit/page.tsx as edit route
    - Fetch existing thought data using GET /thoughts/{id} endpoint
    - Display content textarea with existing content
    - Add status dropdown with three options: APPROVED, REMOVED, IN_REVIEW
    - Show current rating statistics as read-only data (thumbs up, thumbs down, percentage)
    - Validate content length: 10 to 500 characters
    - Use react-hook-form for form management
    - Call PUT /thoughts/{id} endpoint with updated content and status
    - Navigate back to list view on successful update
    - Show success toast notification
  - [x] 3.7 Implement delete functionality with confirmation
    - Add delete button to each table row
    - Show confirmation dialog using shadcn/ui AlertDialog before deletion
    - Include thought content preview in confirmation dialog
    - Call DELETE /thoughts/{id} endpoint on confirmation
    - Remove thought from local state on successful deletion
    - Refresh table data after deletion
    - Display error toast message if deletion fails
  - [x] 3.8 Add loading states for all API operations
    - Implement loading skeleton for table view using shadcn/ui
    - Add loading spinner for form submissions
    - Disable form buttons during submission to prevent duplicate requests
    - Show loading state during delete operation
  - [x] 3.9 Implement error handling for all API operations
    - Handle network errors with user-friendly messages
    - Handle 404 responses for missing thoughts
    - Handle 400 Bad Request responses for validation errors
    - Display errors using shadcn/ui Toast component
    - Show inline validation errors from backend in forms
  - [x] 3.10 Ensure admin CRUD tests pass
    - Run ONLY the 2-8 tests written in 3.1
    - Verify critical CRUD operations work
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 3.1 pass
- Thoughts list displays with all required columns and pagination
- Rating percentage calculated correctly with visual indicator
- Create form validates and submits new thoughts with IN_REVIEW status
- Edit form updates thought content and status
- Delete functionality works with confirmation dialog
- Loading states shown during all operations
- Error handling displays user-friendly messages
- Utilitarian design implemented consistently

### User-Facing Application - Project Setup and Core Features

#### Task Group 4: msa-ai-frontend Next.js Application Setup
**Dependencies:** Task Group 1 (requires status field in API responses)

- [x] 4.0 Complete msa-ai-frontend project initialization
  - [x] 4.1 Initialize Next.js 14+ project with TypeScript
    - Create new Next.js project with TypeScript template
    - Enable App Router (not Pages Router)
    - Configure project name as msa-ai-frontend
    - Verify project structure uses /app directory
  - [x] 4.2 Configure Tailwind CSS
    - Install Tailwind CSS with PostCSS and Autoprefixer
    - Configure tailwind.config.js with content paths for /app directory
    - Set up global styles in app/globals.css with focus on typography
    - Configure larger font sizes for thought display
  - [x] 4.3 Install and configure shadcn/ui component library
    - Initialize shadcn/ui with Radix UI primitives
    - Install core shadcn/ui components: Button, Toast, Skeleton
    - Configure components directory structure
    - Verify component imports work correctly
  - [x] 4.4 Configure standalone output mode for Node.js deployment
    - Set output: 'standalone' in next.config.js
    - Configure environment variables structure
    - Create .env.local template file
    - Document deployment configuration in README
  - [x] 4.5 Set up API client service for backend communication
    - Create lib/api-client.ts with fetch-based service
    - Configure base URL from environment variable (NEXT_PUBLIC_API_BASE_URL)
    - Implement error handling wrapper for API calls
    - Create TypeScript interfaces for Thought entity matching backend schema
    - Include endpoints: GET /thoughts/random, POST /thoughts/thumbsup/{id}, POST /thoughts/thumbsdown/{id}
  - [x] 4.6 Create project documentation
    - Document project structure in README
    - Document environment variables required
    - Document API integration patterns
    - Include local development setup instructions

**Acceptance Criteria:**
- Next.js 14+ project initialized with TypeScript and App Router
- Tailwind CSS configured with typography-focused styles
- shadcn/ui components installed and accessible
- Standalone output mode configured in next.config.js
- API client service created with random thought and rating endpoints
- Environment variables configured for backend URL
- README documentation complete

#### Task Group 5: Random Thought Display and Rating System
**Dependencies:** Task Group 4

- [x] 5.0 Complete user-facing thought display and rating functionality
  - [x] 5.1 Write 2-8 focused tests for random thought display and rating
    - Test random thought fetches and displays on page load
    - Test thumbs up button updates UI state with visual feedback
    - Test thumbs down button updates UI state with visual feedback
    - Test "View Another Thought" button fetches new thought
    - Test rating buttons disabled after selection
    - Limit to 2-8 highly focused tests maximum
    - Skip exhaustive testing of all UI states and error scenarios
  - [x] 5.2 Create main thought display page
    - Create app/page.tsx as main route
    - Fetch random thought on page load using GET /thoughts/random endpoint
    - Display thought content prominently in center of page
    - Use large, readable typography (text-2xl or larger)
    - Follow polished, user-friendly design approach
    - Center content vertically and horizontally on page
    - Use single-page application pattern (no navigation)
  - [x] 5.3 Implement thumbs up and thumbs down buttons
    - Create rating buttons below thought content using shadcn/ui Button component
    - Use thumb icons (thumbs up and thumbs down)
    - Position buttons side by side with clear visual distinction
    - Default state: both buttons outlined/ghost style
    - Ensure buttons are accessible with proper ARIA labels
  - [x] 5.4 Implement rating functionality with API integration
    - Thumbs up button calls POST /thoughts/thumbsup/{id} endpoint
    - Thumbs down button calls POST /thoughts/thumbsdown/{id} endpoint
    - Use React state to track current rating selection
    - Display API call loading state during rating submission
    - Handle API errors with toast notification
  - [x] 5.5 Add visual feedback for rating selection
    - On thumbs up click: change button to solid/filled style with color
    - On thumbs up click: grey out thumbs down button
    - On thumbs down click: change button to solid/filled style with color
    - On thumbs down click: grey out thumbs up button
    - Disable both buttons after selection to prevent duplicate ratings
    - Apply visual feedback immediately on click (optimistic UI update)
  - [x] 5.6 Implement "View Another Thought" functionality
    - Create "View Another Thought" button below rating buttons
    - On click, fetch new random thought from GET /thoughts/random endpoint
    - Replace current thought content without navigation or route change
    - Reset rating button states to default (both enabled, no selection)
    - Show loading state during fetch using shadcn/ui Skeleton or spinner
    - Update page in-place with smooth content replacement
  - [x] 5.7 Add loading states for thought fetching
    - Show skeleton loader for initial page load
    - Show loading indicator when fetching new random thought
    - Display loading state for rating button clicks
    - Ensure smooth transitions between loading and content states
  - [x] 5.8 Implement error handling for API operations
    - Handle network errors when fetching random thoughts
    - Handle case where no thoughts are available (404 response)
    - Handle rating submission errors
    - Display errors using shadcn/ui Toast component
    - Provide retry option for failed fetches
  - [x] 5.9 Apply polished UI design and styling
    - Use engaging color scheme for rating buttons
    - Add smooth hover states and transitions
    - Ensure responsive design works on mobile, tablet, and desktop
    - Add subtle animations for thought content changes
    - Polish typography with appropriate line height and spacing
    - Create visually appealing layout focused on user experience
  - [x] 5.10 Ensure user-facing frontend tests pass
    - Run ONLY the 2-8 tests written in 5.1
    - Verify critical user workflows work
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 5.1 pass
- Random thought displays on page load
- Thumbs up and thumbs down buttons functional
- Visual feedback applied on rating selection (solid color, grey out opposite)
- Both buttons disabled after rating to prevent duplicates
- "View Another Thought" button fetches and displays new thought in-place
- Rating states reset when viewing another thought
- Loading states shown during all operations
- Error handling displays user-friendly messages
- Polished, engaging UI design implemented

## Execution Order

Recommended implementation sequence:

1. **Backend Enhancement** (Task Group 1)
   - Complete status field addition to Thought entity and database
   - This enables both frontend applications to use status data

2. **Admin Application Setup** (Task Group 2)
   - Initialize msa-ai-admin Next.js project with all dependencies
   - Establishes foundation for admin CRUD features

3. **Admin Application CRUD** (Task Group 3)
   - Build complete thought management functionality
   - This can be developed in parallel with user-facing frontend

4. **User-Facing Application Setup** (Task Group 4)
   - Initialize msa-ai-frontend Next.js project with all dependencies
   - Can be started in parallel with Task Group 3

5. **User-Facing Application Features** (Task Group 5)
   - Build random thought display and rating system
   - Complete final user-facing functionality

**Parallel Development Opportunities:**
- Task Groups 2 and 4 (both application setups) can be completed in parallel
- Task Groups 3 and 5 (admin CRUD and user features) can be developed in parallel after their respective setup groups complete

## Testing Strategy

**Focused Testing Approach:**
- Each task group writes 2-8 focused tests maximum during development
- Tests cover only critical behaviors and primary user workflows
- Test verification runs ONLY newly written tests, not entire suite
- No dedicated test gap analysis group needed (sufficient coverage from task groups)
- Total expected tests: approximately 10-24 tests across all groups

**Testing Focus Areas:**
- Backend entity: status enum persistence and validation
- Admin CRUD: list rendering, create, edit, delete operations
- User frontend: thought display, rating functionality, state management

**Test Execution:**
- Each task group ends with running only its own tests
- Full test suite run deferred until final verification phase
- Focus on fast feedback loops during development
