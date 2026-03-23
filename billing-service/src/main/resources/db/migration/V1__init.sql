CREATE TABLE billing_periods (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    client_name_snapshot VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_billing_period UNIQUE (client_id, start_date, end_date)
);

CREATE TABLE billing_summaries (
    id UUID PRIMARY KEY,
    billing_period_id UUID NOT NULL REFERENCES billing_periods(id),
    client_id UUID NOT NULL,
    client_name_snapshot VARCHAR(255) NOT NULL,
    project_id UUID NOT NULL,
    project_name_snapshot VARCHAR(255) NOT NULL,
    consultant_id UUID NOT NULL,
    consultant_name_snapshot VARCHAR(255) NOT NULL,
    approved_hours NUMERIC(8, 2) NOT NULL,
    hourly_rate NUMERIC(12, 2) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE billing_audit_events (
    id UUID PRIMARY KEY,
    actor_id UUID NOT NULL,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255) NOT NULL,
    entity_id UUID NOT NULL,
    metadata VARCHAR(4000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_billing_summaries_period_id ON billing_summaries(billing_period_id);
CREATE INDEX idx_billing_summaries_client_id ON billing_summaries(client_id);
CREATE INDEX idx_billing_summaries_project_id ON billing_summaries(project_id);
CREATE INDEX idx_billing_summaries_consultant_id ON billing_summaries(consultant_id);
CREATE INDEX idx_billing_audit_events_created_at ON billing_audit_events(created_at);
