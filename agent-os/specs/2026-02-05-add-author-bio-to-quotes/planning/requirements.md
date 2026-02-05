# Spec Requirements: Add Author Bio to Quotes

## Initial Description
For a new spec that adds the author and author's bio to the quotes.

## Requirements Discussion

### First Round Questions

**Q1:** I assume you want to add "author" and "author_bio" fields to the existing "thoughts" database table in PostgreSQL. Is that correct, or should we create a separate "authors" table with a relationship to thoughts?
**Answer:** Yes, add "author" and "author_bio" fields to the existing "thoughts" database table in PostgreSQL.

**Q2:** I'm thinking the author field would be a simple text field (VARCHAR) for the author's name, and the author_bio would be a larger text field for the biography. Should we impose any character limits (e.g., 200 characters for name, 1000 for bio)?
**Answer:** 200 characters is fine for both author name and author bio.

**Q3:** For existing thoughts in the database that don't have an author, should these fields be optional (nullable), or should we require all thoughts to have an author going forward?
**Answer:** Instead of nullable, use "Unknown" as the default value for thoughts with no author.

**Q4:** On the frontend, I assume we should display the author name prominently with each thought, and show the bio in a tooltip, popover, or expandable section. Which approach would work best for your use case?
**Answer:** The author and bio should be below the thought and in a smaller font.

**Q5:** When creating or editing a thought, should users be able to input both the author name and bio directly in the form, or should there be a separate author management interface?
**Answer:** Users should be able to input both the author name and bio directly in the thought creation/editing form.

**Q6:** Should the author bio be displayed on the random thought display page, the rating interface, or both? Should it also appear when thoughts are evaluated by AI?
**Answer:** Author bio should be displayed on both the random thought display page and the rating interface. It should also appear when thoughts are evaluated by AI.

**Q7:** For the AI evaluation service, should the LLM also consider the author and bio when evaluating thoughts, or should these fields be purely informational for users?
**Answer:** The AI should NOT consider the author and bio when evaluating thoughts.

**Q8:** Are there any features or behaviors you specifically want to exclude from this implementation? For example, should we avoid author profiles, author search functionality, or multiple authors per thought?
**Answer:** Avoid author profiles, author search functionality, and multiple authors per thought.

### Existing Code to Reference

**Similar Features Identified:**
- Quarkus REST Application: `thoughts-msa-ai-backend`
- User-facing application: `msa-ai-frontend`
- Creation and moderation site: `msa-ai-admin`

### Follow-up Questions

**Follow-up 1:** For the database migration, should we set "Unknown" as the default value at the database level, or handle it in the application code when creating new thoughts? This affects whether the database constraint will be NOT NULL with DEFAULT 'Unknown' or if we'll handle the default in the Quarkus entity/service layer.
**Answer:** Handle it in the Quarkus application code (not at the database level). The entity/service layer should set "Unknown" as the default when creating new thoughts.

**Follow-up 2:** When you say the author and bio should be "below the thought and in a smaller font," should they be displayed together (e.g., "- Author Name: Bio text here") or on separate lines? Should the author name have any special formatting like bold or italic?
**Answer:** Display author and bio on the same line, for example: "Hunter S. Thompson, Author and Journalist" (author name followed by comma, then bio text).

**Follow-up 3:** For the AI evaluation service, since the LLM should NOT consider the author and bio when evaluating thoughts, should we explicitly exclude these fields from the data sent to the LLM, or will the existing evaluation logic naturally ignore them?
**Answer:** Send the entire thought (including author and bio) to the application calling the LLM, which hasn't been built yet. That application will handle sending appropriate data to the LLM.

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
No visual files found.

## Requirements Summary

### Functional Requirements
- Add "author" and "author_bio" fields to the existing "thoughts" table in PostgreSQL
- Both fields limited to 200 characters each
- Default value of "Unknown" for both fields, handled in Quarkus application code (not database constraint)
- Update thought creation/editing forms to include author name and bio input fields
- Display author and bio below each thought in smaller font, formatted as: "Author Name, Bio text"
- Show author and bio on random thought display page
- Show author and bio on rating interface
- Include author and bio when sending thoughts to AI evaluation service
- AI evaluation application (to be built) will determine what data to send to the LLM

### Reusability Opportunities
- Existing thought entity models in `thoughts-msa-ai-backend`
- Existing form patterns in `msa-ai-admin` for thought creation/editing
- Existing display components in `msa-ai-frontend` for showing thoughts
- Existing REST API patterns in `thoughts-msa-ai-backend`

### Scope Boundaries
**In Scope:**
- Database schema changes to add author and author_bio fields
- Quarkus entity and service layer updates
- REST API updates to include author fields
- Frontend form updates for inputting author information
- Frontend display updates to show author and bio on random thought page
- Frontend display updates to show author and bio on rating interface
- Include author and bio in data sent to AI evaluation service

**Out of Scope:**
- Separate author profiles or author management interface
- Author search functionality
- Multiple authors per thought
- Determining exactly what the AI evaluation service sends to the LLM (that service will handle this logic)

### Technical Considerations
- PostgreSQL database migration needed to add columns
- Hibernate/Panache entity updates in Quarkus
- Default value handling in application code (not database level)
- REST endpoint modifications to support new fields
- Next.js frontend form updates in admin interface
- Next.js frontend display updates in user-facing application
- Event schema updates for Kafka events (thought creation/update events)
- Integration with existing thought creation and rating workflows
- Follow existing patterns in `thoughts-msa-ai-backend`, `msa-ai-frontend`, and `msa-ai-admin`
