package app.web.dto;

import app.domain.Task;

public record UpdateTaskRequest(
        String title,
        Task.State state,
        Task.Priority priority
) {
}
