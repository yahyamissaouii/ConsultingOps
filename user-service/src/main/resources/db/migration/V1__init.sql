CREATE TABLE users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE consultants (
    id UUID PRIMARY KEY,
    employee_code VARCHAR(100) NOT NULL UNIQUE,
    job_title VARCHAR(255) NOT NULL,
    seniority_level VARCHAR(50) NOT NULL,
    hourly_rate NUMERIC(12, 2) NOT NULL,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE clients (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    billing_address VARCHAR(1000) NOT NULL,
    tax_identifier VARCHAR(255) UNIQUE,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    client_id UUID NOT NULL REFERENCES clients(id),
    start_date DATE NOT NULL,
    end_date DATE,
    billing_model VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE project_assignments (
    id UUID PRIMARY KEY,
    consultant_id UUID NOT NULL REFERENCES consultants(id),
    project_id UUID NOT NULL REFERENCES projects(id),
    assigned_role VARCHAR(255) NOT NULL,
    allocation_percentage INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_id UUID,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    metadata VARCHAR(4000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_projects_client_id ON projects(client_id);
CREATE INDEX idx_assignments_consultant_id ON project_assignments(consultant_id);
CREATE INDEX idx_assignments_project_id ON project_assignments(project_id);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
