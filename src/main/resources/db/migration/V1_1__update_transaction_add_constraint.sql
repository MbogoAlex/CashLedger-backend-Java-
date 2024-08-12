ALTER TABLE transaction
ADD CONSTRAINT unique_transaction_code UNIQUE (transaction_code);
