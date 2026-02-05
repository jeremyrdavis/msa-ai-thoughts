-- Migration: Add author and author_bio columns to thoughts table
-- Description: Adds author name and biographical information fields to support attribution
-- Reversible: Yes (see rollback script)

-- Add author column
ALTER TABLE thoughts
ADD COLUMN author VARCHAR(200);

-- Add author_bio column
ALTER TABLE thoughts
ADD COLUMN author_bio VARCHAR(200);

-- Update existing rows to have "Unknown" for both author fields
UPDATE thoughts
SET author = 'Unknown'
WHERE author IS NULL;

UPDATE thoughts
SET author_bio = 'Unknown'
WHERE author_bio IS NULL;

-- Set NOT NULL constraints after data population
ALTER TABLE thoughts
ALTER COLUMN author SET NOT NULL;

ALTER TABLE thoughts
ALTER COLUMN author_bio SET NOT NULL;

-- Rollback script (to be executed manually if needed):
-- ALTER TABLE thoughts DROP COLUMN author_bio;
-- ALTER TABLE thoughts DROP COLUMN author;
