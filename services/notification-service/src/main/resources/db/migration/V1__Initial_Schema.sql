-- Notification Service - Initial Schema
-- Created: 2026-03-09

-- ======================== NOTIFICATIONS TABLE ========================
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(255),
    subject VARCHAR(500) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, FAILED, BOUNCED
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    sent_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'BOUNCED'))
);

CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_recipient_email ON notifications(recipient_email);
CREATE INDEX idx_notifications_event_type ON notifications(event_type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- ======================== NOTIFICATION_VARIABLES TABLE ========================
-- Store template variables separately for flexibility and analysis
CREATE TABLE notification_variables (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    variable_key VARCHAR(100) NOT NULL,
    variable_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_vars_notification_id ON notification_variables(notification_id);

-- ======================== DEAD_LETTER_QUEUE TABLE ========================
-- Failed emails that need manual intervention
CREATE TABLE dead_letter_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID REFERENCES notifications(id),
    event_type VARCHAR(100) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    error_message TEXT,
    original_payload TEXT,
    retry_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

CREATE INDEX idx_dlq_notification_id ON dead_letter_queue(notification_id);
CREATE INDEX idx_dlq_retry_available ON dead_letter_queue(retry_available);
CREATE INDEX idx_dlq_created_at ON dead_letter_queue(created_at);

-- ======================== AUDIT_LOG TABLE ========================
-- Track all notification service operations for compliance and debugging
CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action VARCHAR(50) NOT NULL, -- CREATED, SENT, FAILED, RETRIED, BOUNCED
    notification_id UUID REFERENCES notifications(id),
    performer VARCHAR(255), -- service or user
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_notification_id ON audit_log(notification_id);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);

-- ======================== UNSUBSCRIBE_LIST TABLE ========================
-- Users who have unsubscribed from notifications
CREATE TABLE unsubscribe_list (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    event_types VARCHAR(500), -- JSON array of excluded events
    reason TEXT,
    unsubscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resubscribed_at TIMESTAMP
);

CREATE INDEX idx_unsubscribe_list_email ON unsubscribe_list(email);
CREATE INDEX idx_unsubscribe_list_resubscribed_at ON unsubscribe_list(resubscribed_at);

-- ======================== NOTIFICATION_TEMPLATES TABLE ========================
-- Cache template metadata for monitoring and validation
CREATE TABLE notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    required_variables VARCHAR(1000), -- JSON array
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO notification_templates (template_name, description, required_variables, version)
VALUES
    ('job-created', 'Notification sent to candidates when new job is posted', '["jobTitle", "companyName", "jobDescription", "jobLink"]', 1),
    ('application-submitted', 'Confirmation sent to candidate when application is submitted', '["candidateName", "jobTitle", "applicationId"]', 1),
    ('interview-invitation', 'Invitation sent for interview', '["candidateName", "jobTitle", "interviewDate", "interviewLink"]', 1),
    ('application-status', 'Status update for application (acceptance, rejection, etc.)', '["candidateName", "jobTitle", "status"]', 1),
    ('job-offer', 'Job offer sent to candidate', '["candidateName", "jobTitle", "companyName", "offerLink"]', 1),
    ('new-application', 'Notification sent to recruiter when new application received', '["recruiterEmail", "candidateName", "jobTitle", "reviewLink"]', 1);

-- ======================== SEQUENCES ========================
-- Optional: For additional ID generation patterns if needed
CREATE SEQUENCE IF NOT EXISTS notification_seq START WITH 1 INCREMENT BY 1;

-- ======================== TRIGGERS & FUNCTIONS ========================
-- Automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_notifications_updated_at
BEFORE UPDATE ON notifications
FOR EACH ROW
EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER update_notification_templates_updated_at
BEFORE UPDATE ON notification_templates
FOR EACH ROW
EXECUTE FUNCTION update_updated_at();
