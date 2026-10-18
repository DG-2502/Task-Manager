CREATE TABLE users
(
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(255) NOT NULL UNIQUE,
    status        VARCHAR(20)  NOT NULL CHECK (status IN ('ADMIN', 'USER')),
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE tasks
(
    id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    start_date DATE         NOT NULL,
    state      VARCHAR(20)  NOT NULL CHECK (state IN ('CLOSED', 'ACTIVE')),
    creator_id INTEGER      NOT NULL,
    CONSTRAINT fk_task_creator FOREIGN KEY (creator_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_tasks_creator_id ON tasks (creator_id);