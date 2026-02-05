-- Migration: Add status column to thoughts table
-- Description: Adds status field to support APPROVED, REMOVED, IN_REVIEW workflow
-- Reversible: Yes (see rollback script)

-- Add status column with default value
ALTER TABLE thoughts
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'IN_REVIEW';

-- Update existing rows to have IN_REVIEW status
UPDATE thoughts
SET status = 'IN_REVIEW'
WHERE status IS NULL OR status = '';

-- Create index on status column for filtering queries
CREATE INDEX idx_thoughts_status ON thoughts(status);

-- Rollback script (to be executed manually if needed):
-- DROP INDEX idx_thoughts_status;
-- ALTER TABLE thoughts DROP COLUMN status;
