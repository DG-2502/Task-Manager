CREATE TABLE task_groups
(
    id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    creator_id INTEGER      NOT NULL,
    CONSTRAINT fk_group_creator FOREIGN KEY (creator_id) REFERENCES users (id) ON DELETE CASCADE
);

ALTER TABLE tasks
    ADD COLUMN group_id INTEGER,
    ADD CONSTRAINT fk_task_group FOREIGN KEY (group_id) REFERENCES task_groups (id) ON DELETE CASCADE;

CREATE INDEX idx_task_groups_creator_id ON task_groups (creator_id);
CREATE INDEX idx_tasks_group_id ON tasks (group_id);