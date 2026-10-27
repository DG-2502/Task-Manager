CREATE INDEX idx_tasks_creator_start_date ON tasks (creator_id, start_date);
CREATE INDEX idx_tasks_creator_priority ON tasks (creator_id, priority);
CREATE INDEX idx_tasks_creator_title ON tasks (creator_id, title);