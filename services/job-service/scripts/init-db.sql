-- Database initialization script for Job Service
-- This script creates the basic schema for the Job Service

-- Create schema
CREATE SCHEMA IF NOT EXISTS jrecruiter;

-- Set search path
SET search_path TO jrecruiter, public;

-- Create jobs table
CREATE TABLE IF NOT EXISTS jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    salary_min DECIMAL(10,2),
    salary_max DECIMAL(10,2),
    salary_currency VARCHAR(3) DEFAULT 'USD',
    location_city VARCHAR(100),
    location_state VARCHAR(50),
    location_country VARCHAR(50),
    industry VARCHAR(100),
    employment_type VARCHAR(50),
    experience_level VARCHAR(50),
    employer_id UUID NOT NULL,
    employer_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT jobs_status_check CHECK (status IN ('OPEN', 'CLOSED', 'DRAFT')),
    CONSTRAINT jobs_employment_type_check CHECK (employment_type IN ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'REMOTE')),
    CONSTRAINT jobs_experience_level_check CHECK (experience_level IN ('ENTRY', 'MID', 'SENIOR', 'EXECUTIVE'))
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_jobs_status ON jobs(status);
CREATE INDEX IF NOT EXISTS idx_jobs_employer ON jobs(employer_id);
CREATE INDEX IF NOT EXISTS idx_jobs_created_at ON jobs(created_at);
CREATE INDEX IF NOT EXISTS idx_jobs_updated_at ON jobs(updated_at);
CREATE INDEX IF NOT EXISTS idx_jobs_location ON jobs(location_city, location_state, location_country);
CREATE INDEX IF NOT EXISTS idx_jobs_industry ON jobs(industry);
CREATE INDEX IF NOT EXISTS idx_jobs_employment_type ON jobs(employment_type);
CREATE INDEX IF NOT EXISTS idx_jobs_experience_level ON jobs(experience_level);

-- Create applications table
CREATE TABLE IF NOT EXISTS applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    candidate_id UUID NOT NULL,
    cover_letter TEXT,
    resume_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'SUBMITTED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT applications_status_check CHECK (status IN ('SUBMITTED', 'REVIEWING', 'INTERVIEWING', 'REJECTED', 'ACCEPTED', 'HIRED'))
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_applications_job ON applications(job_id);
CREATE INDEX IF NOT EXISTS idx_applications_candidate ON applications(candidate_id);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);
CREATE INDEX IF NOT EXISTS idx_applications_created_at ON applications(created_at);

-- Create statistics table
CREATE TABLE IF NOT EXISTS job_statistics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    view_count INTEGER DEFAULT 0,
    application_count INTEGER DEFAULT 0,
    unique_visitors INTEGER DEFAULT 0,
    last_viewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_job_statistics_job ON job_statistics(job_id);

-- Create search index table (for CQRS pattern)
CREATE TABLE IF NOT EXISTS job_search_index (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL UNIQUE REFERENCES jobs(id) ON DELETE CASCADE,
    business_name VARCHAR(255) NOT NULL,
    job_title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    location JSONB,
    salary JSONB,
    industry VARCHAR(100),
    employment_type VARCHAR(50),
    experience_level VARCHAR(50),
    status VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    indexed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for search
CREATE INDEX IF NOT EXISTS idx_job_search_title ON job_search_index USING gin(to_tsvector('english', job_title));
CREATE INDEX IF NOT EXISTS idx_job_search_description ON job_search_index USING gin(to_tsvector('english', description));
CREATE INDEX IF NOT EXISTS idx_job_search_location ON job_search_index USING gin(location);
CREATE INDEX IF NOT EXISTS idx_job_search_salary ON job_search_index USING gin(salary);
CREATE INDEX IF NOT EXISTS idx_job_search_industry ON job_search_index(industry);
CREATE INDEX IF NOT EXISTS idx_job_search_status ON job_search_index(status);

-- Create triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_jobs_updated_at BEFORE UPDATE ON jobs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_applications_updated_at BEFORE UPDATE ON applications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_job_statistics_updated_at BEFORE UPDATE ON job_statistics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_job_search_index_updated_at BEFORE UPDATE ON job_search_index
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data
INSERT INTO jobs (id, job_title, description, salary_min, salary_max, salary_currency, location_city, location_state, location_country, industry, employment_type, experience_level, employer_id, employer_name, status, created_at, published_at)
VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Software Engineer', 'We are looking for a talented software engineer to join our team. You will work on cutting-edge technologies and collaborate with a passionate team.', 80000.00, 120000.00, 'USD', 'San Francisco', 'CA', 'USA', 'Technology', 'FULL_TIME', 'MID', 'employer-001', 'Tech Company Inc.', 'OPEN', '2024-01-15 10:00:00-05', '2024-01-15 10:00:00-05'),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Data Scientist', 'Data Scientist needed to analyze large datasets and build predictive models. Experience with machine learning and statistical analysis required.', 90000.00, 140000.00, 'USD', 'New York', 'NY', 'USA', 'Technology', 'FULL_TIME', 'SENIOR', 'employer-002', 'Data Analytics Corp.', 'OPEN', '2024-01-16 14:30:00-05', '2024-01-16 14:30:00-05'),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Product Manager', 'Product Manager to lead product development from ideation to launch. Strong communication skills and experience with agile methodologies required.', 100000.00, 150000.00, 'USD', 'Austin', 'TX', 'USA', 'Technology', 'FULL_TIME', 'MID', 'employer-003', 'Product Solutions LLC.', 'OPEN', '2024-01-17 09:15:00-05', '2024-01-17 09:15:00-05');

INSERT INTO applications (id, job_id, candidate_id, cover_letter, status, created_at, submitted_at)
VALUES
    ('app-001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'candidate-001', 'I am excited about this opportunity and believe my skills match the requirements.', 'SUBMITTED', '2024-01-15 11:00:00-05', '2024-01-15 11:00:00-05'),
    ('app-002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'candidate-002', 'With my experience in data science, I am confident I can contribute to your team.', 'SUBMITTED', '2024-01-16 15:00:00-05', '2024-01-16 15:00:00-05');

INSERT INTO job_statistics (id, job_id, view_count, application_count, unique_visitors, last_viewed_at)
VALUES
    ('stat-001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 150, 5, 120, '2024-01-18 10:00:00-05'),
    ('stat-002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 200, 8, 180, '2024-01-18 14:00:00-05'),
    ('stat-003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 100, 3, 90, '2024-01-18 16:00:00-05');

INSERT INTO job_search_index (id, job_id, business_name, job_title, description, location, salary, industry, employment_type, experience_level, status, created_at, updated_at)
VALUES
    ('idx-001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Tech Company Inc.', 'Software Engineer', 'We are looking for a talented software engineer to join our team. You will work on cutting-edge technologies and collaborate with a passionate team.', '{\"city\": \"San Francisco\", \"state\": \"CA\", \"country\": \"USA\"}', '{\"min\": 80000, \"max\": 120000, \"currency\": \"USD\"}', 'Technology', 'FULL_TIME', 'MID', 'OPEN', '2024-01-15 10:00:00-05', '2024-01-15 10:00:00-05'),
    ('idx-002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Data Analytics Corp.', 'Data Scientist', 'Data Scientist needed to analyze large datasets and build predictive models. Experience with machine learning and statistical analysis required.', '{\"city\": \"New York\", \"state\": \"NY\", \"country\": \"USA\"}', '{\"min\": 90000, \"max\": 140000, \"currency\": \"USD\"}', 'Technology', 'FULL_TIME', 'SENIOR', 'OPEN', '2024-01-16 14:30:00-05', '2024-01-16 14:30:00-05'),
    ('idx-003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Product Solutions LLC.', 'Product Manager', 'Product Manager to lead product development from ideation to launch. Strong communication skills and experience with agile methodologies required.', '{\"city\": \"Austin\", \"state\": \"TX\", \"country\": \"USA\"}', '{\"min\": 100000, \"max\": 150000, \"currency\": \"USD\"}', 'Technology', 'FULL_TIME', 'MID', 'OPEN', '2024-01-17 09:15:00-05', '2024-01-17 09:15:00-05');