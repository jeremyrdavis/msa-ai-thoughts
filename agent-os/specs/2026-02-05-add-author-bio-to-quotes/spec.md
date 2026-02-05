# Specification: Add Author and Author Bio to Quotes

## Goal
Extend the thoughts system to include author name and biographical information for each thought, displayed alongside the thought content across all interfaces.

## User Stories
- As a content creator, I want to add author name and bio when creating thoughts so that proper attribution is maintained
- As a user viewing thoughts, I want to see who said each thought and their bio so that I understand the context and credibility of the quote

## Specific Requirements

**Database Schema Extension**
- Add "author" VARCHAR(200) column to thoughts table
- Add "author_bio" VARCHAR(200) column to thoughts table
- Create database migration using Flyway naming convention (V2__add_author_fields.sql)
- Include rollback script comments in migration file for reversibility
- Both fields should allow NULL at database level but will be handled by application logic

**Thought Entity Model Updates**
- Add author field to Thought entity with @NotBlank validation and @Size(max=200)
- Add authorBio field to Thought entity with @NotBlank validation and @Size(max=200)
- Set default value "Unknown" for both fields in @PrePersist method when values are null or empty
- Follow existing pattern used for status field default handling in prePersist()

**REST API Changes**
- Update ThoughtResource POST endpoint to accept author and authorBio in request body
- Update ThoughtResource PUT endpoint to allow updating author and authorBio fields
- Ensure all GET endpoints return author and authorBio fields in response JSON
- Follow existing validation pattern using @Valid annotation on request objects

**Kafka Event Updates**
- ThoughtEventService should include author and authorBio when publishing to thoughts-events channel
- Ensure Thought objects sent to Kafka contain complete data including author fields
- No changes needed to event publishing logic, just ensure entity contains new fields

**Admin Interface Form Updates**
- Add author input field to thought creation form (app/thoughts/new/page.tsx)
- Add authorBio input field to thought creation form
- Use shadcn/ui Input component for author field with character counter showing x/200
- Use shadcn/ui Input component for authorBio field with character counter showing x/200
- Update Zod schema to validate min 1, max 200 characters for both fields
- Position author fields below content textarea, before submit buttons
- Update apiClient.createThought to include author and authorBio in request payload

**Admin Interface Edit Form Updates**
- Add author and authorBio fields to thought edit form (app/thoughts/[id]/edit/page.tsx)
- Pre-populate fields with existing values when editing
- Follow same validation and UI patterns as creation form
- Update apiClient.updateThought to include author and authorBio in request payload

**Frontend Display Updates**
- Update user-facing page (msa-ai-frontend/app/page.tsx) to display author and bio below thought content
- Format as single line: "Author Name, Bio Text" in smaller font size (text-sm or text-base vs thought's text-2xl/3xl/4xl)
- Add appropriate spacing between thought content and author line (mt-4 or mt-6)
- Apply muted text color (text-zinc-600 dark:text-zinc-400) for author attribution
- Center-align author text to match thought content alignment
- Include author display in both loading states and actual content display

**TypeScript Type Updates**
- Add author: string to Thought interface in both msa-ai-admin and msa-ai-frontend lib/types.ts
- Add authorBio: string to Thought interface in both frontend applications
- Update CreateThoughtRequest interface to include author and authorBio
- Update UpdateThoughtRequest interface to include author and authorBio
- Ensure type consistency across admin and user-facing applications

**AI Evaluation Integration**
- Ensure author and authorBio are included in Thought JSON when sent to AI evaluation endpoints
- No filtering or exclusion of author fields from evaluation service requests
- Future AI application will handle determining what data to send to LLM
- Keep complete thought data flowing through the system

## Visual Design

No visual assets provided.

## Existing Code to Leverage

**Thought Entity Model (thoughts-msa-ai-backend/src/main/java/com/redhat/demos/thoughts/model/Thought.java)**
- Uses Panache entity pattern extending PanacheEntityBase
- Implements @PrePersist for setting default values (status field pattern to replicate)
- Uses validation annotations (@NotBlank, @Size) for field constraints
- Has createdAt/updatedAt timestamp management to follow

**Migration Pattern (thoughts-msa-ai-backend/src/main/resources/db/migration/V1__add_status_column.sql)**
- Follows Flyway naming convention V[number]__[description].sql
- Includes descriptive comments explaining migration purpose
- Contains rollback script in comments for reference
- Creates indexes for frequently queried columns

**Thought Form Pattern (msa-ai-admin/app/thoughts/new/page.tsx)**
- Uses react-hook-form with Zod schema validation
- Implements character counter display pattern (contentLength / 500 characters)
- Uses shadcn/ui form components (Form, FormField, FormItem, FormLabel, FormControl, FormMessage)
- Shows validation feedback with FormDescription for character limits
- Handles async submission with loading states and toast notifications

**Frontend Display Component (msa-ai-frontend/app/page.tsx)**
- Displays thought content in large centered text with responsive sizing
- Uses Tailwind gradient background and card-based layout
- Implements loading states with Skeleton components
- Has rating interface below thought content that can serve as reference for author placement

**API Client Pattern (msa-ai-admin/lib/api-client.ts)**
- Centralized API communication with typed request/response interfaces
- Error handling with custom ApiError class
- Uses handleResponse utility for consistent response processing
- Environment variable for API base URL configuration

## Out of Scope
- Separate authors table or author entity model
- Author management interface or dedicated author CRUD operations
- Author profile pages or detailed author views
- Author search or filtering functionality
- Multiple authors per thought or co-author support
- Author relationships or author-to-author connections
- Autocomplete or author suggestion features
- Author image or avatar support
- Import/export of author data separately from thoughts
- Historical tracking of author changes or audit log for author modifications
