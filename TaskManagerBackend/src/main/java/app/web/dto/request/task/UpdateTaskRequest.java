package app.web.dto.request.task;

import app.domain.Task;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @Size(max = 255, message = "Title is too long")
        String title,
        Task.State state,
        Task.Priority priority
) {
}
