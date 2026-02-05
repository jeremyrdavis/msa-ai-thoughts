# Spec Requirements: Frontend Application

## Initial Description
For 3. Frontend Application. Let's create 2 different applications: msa-ai-admin, which will contain the admin functionality and msa-ai-frontend, which will contain the user facing functionality

## Requirements Discussion

### First Round Questions

**Q1:** I assume msa-ai-admin will handle CRUD operations for managing thoughts (create, edit, delete thoughts) while msa-ai-frontend will be the public-facing display showing thoughts, ratings, and AI evaluations. Is that correct, or should both applications support different use cases?
**Answer:** Correct. msa-ai-frontend only needs to display a random thought with "thumbs up" and "thumbs down" buttons/icons and a button to view another thought.

**Q2:** I'm thinking both applications will be built with Next.js compiled to static sites and served by separate Quarkus microservices (following your tech stack). Should we have two separate Quarkus services (one for each frontend), or a single Quarkus service that serves both static sites on different routes?
**Answer:** User asked for pros/cons of serving static sites with NodeJS instead of Quarkus. Decision made to table the Quarkus apps for serving the static sites for now and use Next.js standalone/Node.js approach instead.

**Q3:** For the admin application (msa-ai-admin), I assume this would require authentication/authorization to restrict access to authorized users only. Should we implement authentication at this stage, or should the admin UI be openly accessible for demo purposes?
**Answer:** Leave the UI open for now. Authorization will be added at a later stage.

**Q4:** Regarding the user-facing frontend (msa-ai-frontend), I assume this will include the random thought display, rating system (thumbs up/down), and AI evaluation display. Should it also show a read-only list of all thoughts, or just random individual thoughts?
**Answer:** Random thoughts only.

**Q5:** For the admin UI, I'm thinking it would include a table/list view of all thoughts with edit/delete actions, plus a form for creating new thoughts. Should it also display rating statistics and AI evaluations for each thought, or focus purely on CRUD operations?
**Answer:** Include rating statistics for each thought and a status which should be: APPROVED, REMOVED, IN_REVIEW. The status should be added as an Enum to the existing Thought Entity.

**Q6:** I assume both applications should use shadcn/ui components and Tailwind CSS for consistency. Should they share a common visual design system, or can they have distinct styling (e.g., admin looks more utilitarian, frontend more polished)?
**Answer:** Utilitarian admin and a more polished, user-friendly frontend.

**Q7:** Regarding API communication, I'm thinking both frontends will call the existing Thoughts Service Backend REST endpoints. Should the admin app have additional admin-specific endpoints, or can it use the same CRUD endpoints already implemented?
**Answer:** Both should call the existing app. The existing endpoints are fine.

**Q8:** Is there anything that should NOT be included in this spec? For example, should we exclude actual authentication implementation, deployment configurations, or specific AI evaluation features at this stage?
**Answer:** Exclude authentication implementation, deployment configurations, and specific AI evaluation features for now.

### Existing Code to Reference

No similar existing features identified for reference.

### Follow-up Questions

**Follow-up 1:** For the admin UI, should the status field (APPROVED, REMOVED, IN_REVIEW) be editable by admins, or is it automatically set based on some criteria? If editable, should it be a dropdown in the table view, or only changeable when editing a thought?
**Answer:** It should be editable. A dropdown is fine, but only when editing a thought (not in the table view).

**Follow-up 2:** You mentioned rating statistics should be displayed in the admin UI. Should this show the total count of thumbs up/down votes for each thought, or more detailed statistics (percentage, ratio, etc.)?
**Answer:** Show the total count for each (thumbs up and thumbs down), and whichever percentage is higher along with the percentage value.

**Follow-up 3:** For the user-facing frontend (msa-ai-frontend), when a user clicks thumbs up or thumbs down, should there be visual feedback (success message, color change, etc.), or should the button press silently record the rating in the background?
**Answer:** Yes, make the chosen thumb a solid color and grey out the other one.

**Follow-up 4:** Regarding the tech stack question about serving static sites - should I document both the Quarkus approach (as per your current tech-stack.md) and the Node.js consideration in the requirements, or wait for your final decision on this architectural choice before documenting requirements?
**Answer:** Table the Quarkus apps for serving the static sites for now. Document Next.js standalone/Node.js approach instead.

**Follow-up 5:** For the "view another thought" button on msa-ai-frontend, should this simply reload/fetch a new random thought on the same page, or navigate to a new page/route?
**Answer:** Yes, simply fetch and reload another thought on the same page (no navigation).

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
No visual assets to analyze.

## Requirements Summary

### Functional Requirements

**msa-ai-admin (Admin Application)**
- Full CRUD operations for managing thoughts
  - Create new thoughts via form
  - Edit existing thoughts (including status field)
  - Delete thoughts
  - List/table view of all thoughts
- Display rating statistics for each thought
  - Total count of thumbs up votes
  - Total count of thumbs down votes
  - Higher percentage value and which rating type is higher
- Status management
  - Add status field to existing Thought Entity as Enum: APPROVED, REMOVED, IN_REVIEW
  - Status editable via dropdown in the edit form (not in table view)
- Utilitarian design using shadcn/ui components and Tailwind CSS

**msa-ai-frontend (User-facing Application)**
- Display random thoughts
  - Single thought view only (no list view)
- Rating system
  - Thumbs up button/icon
  - Thumbs down button/icon
  - Visual feedback: selected thumb becomes solid color, other thumb greyed out
  - Ratings recorded via API call
- View another thought button
  - Fetches new random thought and reloads on same page (no navigation)
- Polished, user-friendly design using shadcn/ui components and Tailwind CSS

**Backend Changes**
- Add status field to Thought Entity as Enum with values: APPROVED, REMOVED, IN_REVIEW
- Existing REST endpoints sufficient for both applications

**Technical Architecture**
- Both applications built with Next.js
- Next.js standalone/Node.js approach for serving (Quarkus serving tabled for now)
- Both applications call existing Thoughts Service Backend REST endpoints
- Both use shadcn/ui component library and Tailwind CSS

### Reusability Opportunities
No similar existing features identified for reference.

### Scope Boundaries

**In Scope:**
- Two separate Next.js applications (msa-ai-admin and msa-ai-frontend)
- Admin UI with full CRUD for thoughts
- Admin UI with rating statistics display
- Status field addition to Thought Entity (APPROVED, REMOVED, IN_REVIEW)
- User-facing UI with random thought display
- Rating system with visual feedback
- Integration with existing Thoughts Service Backend API
- shadcn/ui components and Tailwind CSS styling
- Distinct visual design approaches (utilitarian admin, polished frontend)

**Out of Scope:**
- Authentication/authorization implementation (to be added later)
- Deployment configurations
- Specific AI evaluation features
- Quarkus services for serving static sites (tabled for now)
- Read-only list of all thoughts in user-facing frontend
- Admin-specific backend endpoints (existing endpoints sufficient)

### Technical Considerations
- Next.js standalone/Node.js serving approach instead of Quarkus
- Integration with existing Thoughts Service Backend REST API
- Status field requires backend Entity modification (Enum addition)
- Both applications use same component library for consistency
- Visual feedback state management for rating buttons in frontend
- Rating statistics calculation for admin display
- Single-page application behavior for user-facing frontend (no navigation on "view another")
