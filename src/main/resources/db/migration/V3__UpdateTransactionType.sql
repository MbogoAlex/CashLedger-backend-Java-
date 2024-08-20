DO $$
BEGIN
   -- Check if the table exists
   IF EXISTS (SELECT 1 FROM information_schema.tables
              WHERE table_schema = 'public'
                AND table_name = 'transaction') THEN

      -- Update transactionType to 'Pochi la Biashara' if recipient does not contain a phone number
      UPDATE transaction
      SET transaction_type = 'Pochi la Biashara'
      WHERE LENGTH(REGEXP_REPLACE(recipient, '[^0-9]', '', 'g')) < 10
        AND entity NOT ILIKE '%SAFARICOM DATA BUNDLES%'
        AND entity NOT ILIKE '%Safaricom%'
        AND entity NOT ILIKE '%Tunukiwa%'
        AND entity NOT ILIKE '%TUNUKIWA%'
        AND entity NOT ILIKE '%Talkmore%'
        AND transaction_type != 'Pay Bill'
        AND transaction_type != 'Buy Goods and Services (till)'
        AND transaction_type != 'Withdraw Cash'
        AND transaction_type != 'Deposit'
        AND transaction_type != 'Lock savings'
        AND transaction_type != 'Hustler Fund'
        AND transaction_type != 'Mshwari'
        AND transaction_type != 'Reversal';

        -- Update transactionType to 'Airtime & Bundles' if recipient contains specific phrases
      UPDATE transaction
      SET transaction_type = 'Airtime & Bundles'
      WHERE entity ILIKE '%SAFARICOM DATA BUNDLES%'
        OR entity ILIKE '%Tunukiwa%'
        OR entity ILIKE '%TUNUKIWA%'
        OR entity ILIKE '%Talkmore%'
        OR transaction_type = 'Airtime';


   END IF;
END $$;
