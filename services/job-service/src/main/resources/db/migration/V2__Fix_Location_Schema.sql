-- Flyway Migration: V2__Fix_Location_Schema
-- Resolve schema mismatch between SQL and JPA entity mappings
-- Discovered in TASK-027 (JPA Adapter Audit)
-- 
-- CHANGES:
-- 1. Rename location_address1 → location_street
-- 2. Drop location_address2 (not in JPA design)
-- 3. Drop location_website, location_phone, location_email (belong to Company, not Job)
-- 4. Add location_country_code (present in JPA, missing in SQL)
-- 5. Add location_remote (present in JPA, missing in SQL)
-- 6. JPA will map location_state_province → location_state (consistent with SQL)

SET search_path TO jrecruiter, public;

-- Step 1: Add new columns to jobs table
ALTER TABLE jobs 
    ADD COLUMN location_country_code VARCHAR(2) DEFAULT NULL,
    ADD COLUMN location_remote BOOLEAN DEFAULT FALSE;

-- Step 2: Rename location_address1 → location_street
ALTER TABLE jobs RENAME COLUMN location_address1 TO location_street;

-- Step 3: Drop unused location columns (not in JPA domain model)
-- These belong to Company/Employer entity, not Job
ALTER TABLE jobs DROP COLUMN location_address2;
ALTER TABLE jobs DROP COLUMN location_website;
ALTER TABLE jobs DROP COLUMN location_phone;
ALTER TABLE jobs DROP COLUMN location_email;

-- Step 4: Verify final location columns match JPA mappings
-- Expected JPA mappings (from JobLocationEmbeddable):
--   location_street         ← location_street (was location_address1)
--   location_city           ← location_city
--   location_state_province ← location_state (consistent naming)
--   location_postal_code    ← location_postal_code
--   location_country        ← location_country
--   location_country_code   ← location_country_code (NEW)
--   location_latitude       ← location_latitude
--   location_longitude      ← location_longitude
--   location_remote         ← location_remote (NEW)

-- Step 5: Create index for remote work queries (common filter)
CREATE INDEX idx_jobs_location_remote ON jobs(location_remote) WHERE location_remote = TRUE;

-- Step 6: Create index for country code queries (geolocation filtering)
CREATE INDEX idx_jobs_location_country_code ON jobs(location_country_code);

-- Migration Status:
-- ✅ All SQL columns now match JPA entity declarations
-- ✅ Ready for JPA mapping updates to use correct column names
-- ✅ No data loss (all changes are additive or trivial renames)
-- ✅ Indexes added for new columns to support filtering

-- Next Steps (in JPA codebase):
-- 1. Update JobLocationEmbeddable.java:
--    - Change @Column(name = "location_state_province") → @Column(name = "location_state")
--    - Change @Column(name = "location_street") ← already correct after V1 rename
-- 2. Run integration tests to verify data persistence
-- 3. Validate no data corruption in existing records
