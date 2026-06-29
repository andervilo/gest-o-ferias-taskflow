-- TaskFlow — schema inicial (Gestão de Colaboradores e Férias)

CREATE TABLE employees (
    id            UUID PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    email         VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(120) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    manager_id    UUID REFERENCES employees(id),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_role CHECK (role IN ('ADMIN', 'MANAGER', 'COLLABORATOR'))
);

CREATE INDEX idx_employees_manager ON employees(manager_id);
CREATE INDEX idx_employees_role    ON employees(role);

CREATE TABLE vacation_requests (
    id          UUID PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES employees(id),
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason      VARCHAR(500),
    decided_by  UUID REFERENCES employees(id),
    decided_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT chk_period CHECK (end_date >= start_date)
);

CREATE INDEX idx_vac_employee      ON vacation_requests(employee_id);
CREATE INDEX idx_vac_status_period ON vacation_requests(status, start_date, end_date);
