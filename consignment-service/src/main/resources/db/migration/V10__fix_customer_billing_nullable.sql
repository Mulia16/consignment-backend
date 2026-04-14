-- Allow customer_code to be null in detail (null = all customers under the store)
ALTER TABLE customer_billing_request_detail
    ALTER COLUMN customer_code DROP NOT NULL;
