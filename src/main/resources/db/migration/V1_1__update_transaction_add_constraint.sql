-- Step 1: Identify Duplicates
-- This query lists all `transaction_code` values that are duplicated.
SELECT transaction_code, COUNT(*)
FROM transaction
GROUP BY transaction_code
HAVING COUNT(*) > 1;

-- Step 2: Remove Duplicates
-- This script removes duplicate rows while keeping the oldest entry for each `transaction_code`.
-- Note: Make sure to execute this script manually to clean up any existing duplicates before applying the constraint.
WITH cte AS (
    SELECT ctid,
           ROW_NUMBER() OVER (PARTITION BY transaction_code ORDER BY id) AS rn
    FROM transaction
)
DELETE FROM transaction
WHERE ctid IN (
    SELECT ctid
    FROM cte
    WHERE rn > 1
);

-- Step 3: Verify Removal
-- After removing duplicates, confirm that no duplicates remain.
-- Run this query to ensure all `transaction_code` values are unique.
SELECT transaction_code, COUNT(*)
FROM transaction
GROUP BY transaction_code
HAVING COUNT(*) > 1;

-- Step 4: Add Unique Constraint
-- Ensure the unique constraint is added to enforce uniqueness of `transaction_code`.
-- This step should be skipped if the constraint was already added in a previous migration.
ALTER TABLE transaction
ADD CONSTRAINT unique_transaction_code UNIQUE (transaction_code);

-- Step 5: Repair Flyway Metadata
-- If Flyway's metadata has inconsistencies due to manual changes, use the `repair` command.
-- Run this command using the Flyway CLI or via build tools like Maven or Gradle:
-- ./mvnw flyway:repair
-- or
-- ./gradlew flywayRepair
