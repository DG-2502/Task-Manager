package app.web.dto.request.task;

import app.domain.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "Title cannot be blank or empty")
        @Size(max = 255, message = "Title is too long")
        String title,
        Task.Priority priority
) {
    public CreateTaskRequest {
        if (priority == null) {
            priority = Task.Priority.MEDIUM;
        }
    }
}
