-- Database migration for Course Expiration and Re-enrollment Discount feature

-- 1. Add access_duration_days to courses table (default 180 days = ~6 months)
ALTER TABLE IF EXISTS courses ADD COLUMN IF NOT EXISTS access_duration_days INTEGER DEFAULT 180;
UPDATE courses SET access_duration_days = 180 WHERE access_duration_days IS NULL;

-- 2. Add expiration_date to enrollments table
ALTER TABLE IF EXISTS enrollments ADD COLUMN IF NOT EXISTS expiration_date TIMESTAMP WITHOUT TIME ZONE;

-- 3. Backfill expiration_date for existing enrollments (enrolled_at + 180 days or current_timestamp + 180 days)
UPDATE enrollments
SET expiration_date = COALESCE(enrolled_at, CURRENT_TIMESTAMP) + INTERVAL '180 days'
WHERE expiration_date IS NULL;

-- 4. Add discount tracking columns to payments table
ALTER TABLE IF EXISTS payments ADD COLUMN IF NOT EXISTS original_amount NUMERIC(19, 2);
ALTER TABLE IF EXISTS payments ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(19, 2) DEFAULT 0.00;
ALTER TABLE IF EXISTS payments ADD COLUMN IF NOT EXISTS is_re_enrollment_discount BOOLEAN DEFAULT FALSE;

UPDATE payments SET original_amount = amount WHERE original_amount IS NULL;
UPDATE payments SET discount_amount = 0.00 WHERE discount_amount IS NULL;
UPDATE payments SET is_re_enrollment_discount = FALSE WHERE is_re_enrollment_discount IS NULL;
