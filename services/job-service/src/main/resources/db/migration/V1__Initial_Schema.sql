-- Flyway Migration: V1__Initial_Schema
-- Create initial schema for Job Service

-- Create schema
CREATE SCHEMA IF NOT EXISTS jrecruiter;
SET search_path TO jrecruiter, public;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create jobs table (main aggregate)
CREATE TABLE IF NOT EXISTS jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    universal_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    employer_id UUID NOT NULL,
    
    -- Core data (immutable)
    job_title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    industry VARCHAR(100),
    region VARCHAR(100),
    
    -- Location (nested)
    location_address1 VARCHAR(255),
    location_address2 VARCHAR(255),
    location_city VARCHAR(100),
    location_state VARCHAR(50),
    location_postal_code VARCHAR(20),
    location_country VARCHAR(50),
    location_website VARCHAR(500),
    location_phone VARCHAR(20),
    location_email VARCHAR(255),
    location_latitude DECIMAL(10,8),
    location_longitude DECIMAL(11,8),
    
    -- Salary (nested)
    salary_min DECIMAL(12,2),
    salary_max DECIMAL(12,2),
    salary_currency VARCHAR(3) DEFAULT 'USD',
    
    -- Status and timestamps
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE,
    
    -- Constraints
    CONSTRAINT job_status_check CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED', 'ARCHIVED')),
    CONSTRAINT job_salary_check CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_min <= salary_max)
);

-- Create indexes for jobs
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_employer_id ON jobs(employer_id);
CREATE INDEX idx_jobs_created_at ON jobs(created_at DESC);
CREATE INDEX idx_jobs_published_at ON jobs(published_at DESC);
CREATE INDEX idx_jobs_city_state ON jobs(location_city, location_state);
CREATE INDEX idx_jobs_industry ON jobs(industry);
CREATE INDEX idx_jobs_universal_id ON jobs(universal_id);

-- Create outbox table (for guaranteed event delivery)
CREATE TABLE IF NOT EXISTS outbox (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    published BOOLEAN DEFAULT FALSE,
    attempts INTEGER DEFAULT 0,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for outbox
CREATE INDEX idx_outbox_published ON outbox(published, created_at);
CREATE INDEX idx_outbox_aggregate ON outbox(aggregate_id, aggregate_type);
CREATE INDEX idx_outbox_event_type ON outbox(event_type);

-- Create processed events table (for idempotent consumers)
CREATE TABLE IF NOT EXISTS processed_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for processed events
CREATE INDEX idx_processed_events_type ON processed_events(event_type);
CREATE INDEX idx_processed_events_at ON processed_events(processed_at);

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA jrecruiter TO job_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA jrecruiter TO job_service;
