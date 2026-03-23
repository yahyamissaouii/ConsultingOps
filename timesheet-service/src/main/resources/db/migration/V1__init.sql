CREATE TABLE time_entries (
    id UUID PRIMARY KEY,
    consultant_id UUID NOT NULL,
    project_id UUID NOT NULL,
    client_id UUID NOT NULL,
    consultant_name_snapshot VARCHAR(255) NOT NULL,
    project_name_snapshot VARCHAR(255) NOT NULL,
    client_name_snapshot VARCHAR(255) NOT NULL,
    hourly_rate_snapshot NUMERIC(12, 2) NOT NULL,
    work_date DATE NOT NULL,
    hours NUMERIC(5, 2) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    billable BOOLEAN NOT NULL,
    status VARCHAR(50) NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE,
    approved_at TIMESTAMP WITH TIME ZONE,
    approved_by UUID,
    rejection_reason VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE time_entry_audits (
    id UUID PRIMARY KEY,
    time_entry_id UUID NOT NULL,
    actor_id UUID NOT NULL,
    action VARCHAR(255) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    note VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_time_entries_consultant_id ON time_entries(consultant_id);
CREATE INDEX idx_time_entries_project_id ON time_entries(project_id);
CREATE INDEX idx_time_entries_client_id ON time_entries(client_id);
CREATE INDEX idx_time_entries_status ON time_entries(status);
CREATE INDEX idx_time_entries_work_date ON time_entries(work_date);
CREATE INDEX idx_time_entry_audits_time_entry_id ON time_entry_audits(time_entry_id);
CREATE INDEX idx_time_entry_audits_created_at ON time_entry_audits(created_at);
