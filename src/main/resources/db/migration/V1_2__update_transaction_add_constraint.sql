-- Ensure the transaction table does not have duplicates
-- Step 1: Remove duplicates while keeping one instance for each transaction_code

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

