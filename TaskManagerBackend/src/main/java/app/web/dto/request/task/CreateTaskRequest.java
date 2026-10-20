package app.web.dto.request.task;

import app.domain.Task;

public record CreateTaskRequest(
        String title,
        Task.Priority priority
) {
    public CreateTaskRequest {
        if (priority == null) {
            priority = Task.Priority.MEDIUM;
        }
    }
}
