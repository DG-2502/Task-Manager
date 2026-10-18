package app.web.dto;

import app.domain.Task;

import java.time.LocalDate;

public record TaskResponse(int id, String title, LocalDate startDate, Task.State state, int creatorId, Task.Priority priority) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getStartDate(),
                task.getState(),
                task.getCreatorId(),
                task.getPriority()
        );
    }
}