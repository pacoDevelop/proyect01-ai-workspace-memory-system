-- V1: Create Candidate and Application Tables
-- Author: antigravity / TASK-014

CREATE TABLE candidates (
    candidate_id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(50),
    skills VARCHAR(2000),
    experience_years INTEGER,
    desired_city VARCHAR(100),
    desired_country VARCHAR(100),
    remote_ok BOOLEAN,
    status VARCHAR(50) NOT NULL,
    bio TEXT,
    registered_at TIMESTAMP NOT NULL,
    profile_completed_at TIMESTAMP,
    suspended_at TIMESTAMP,
    deactivated_at TIMESTAMP
);

CREATE INDEX idx_candidates_email ON candidates(email);
CREATE INDEX idx_candidates_status ON candidates(status);

CREATE TABLE applications (
    application_id UUID PRIMARY KEY,
    candidate_id UUID NOT NULL,
    job_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    cover_letter TEXT,
    submitted_at TIMESTAMP NOT NULL,
    status_changed_at TIMESTAMP,
    rejection_reason VARCHAR(255),
    CONSTRAINT fk_application_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id)
);

CREATE INDEX idx_applications_candidate_id ON applications(candidate_id);
CREATE INDEX idx_applications_job_id ON applications(job_id);
CREATE INDEX idx_applications_status ON applications(status);
