DO $$
BEGIN
   -- Check if the table exists
   IF EXISTS (SELECT 1 FROM information_schema.tables
              WHERE table_schema = 'public'
                AND table_name = 'transaction') THEN

      -- Update transactionType to 'Airtime & Bundles' if recipient contains specific phrases
      UPDATE transaction
      SET transaction_type = 'Send Money'
      WHERE transaction_type = 'Send money';

      UPDATE transaction
      SET transaction_type = 'Mshwari'
      WHERE transaction_type = 'Lock savings';

   END IF;
END $$;
