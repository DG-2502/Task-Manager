ALTER TABLE tasks
    ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' CHECK (priority in ('LOW', 'MEDIUM', 'HIGH'));

CREATE INDEX idx_tasks_priority ON tasks (priority);