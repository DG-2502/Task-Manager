package app.web.dto.request.task;

import app.domain.Task;

public record UpdateTaskRequest(
        String title,
        Task.State state,
        Task.Priority priority
) {
}
