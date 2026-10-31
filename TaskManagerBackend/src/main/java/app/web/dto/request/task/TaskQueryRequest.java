package app.web.dto.request.task;

import app.domain.Task;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Sort;

public record TaskQueryRequest(
        @Min(value = 0, message = "Page cannot be negative")
        Integer page,
        @Min(value = 1, message = "Size must be at least 1")
        @Max(value = 100, message = "Size cannot exceed 100")
        Integer size,

        Task.State state,
        Task.Priority priority,
        SortField sortBy,
        Sort.Direction direction,
        @Size(max = 255, message = "String is too long")
        String search
) {
    public enum SortField {
        ID, TITLE, STARTDATE, PRIORITY
    }

    public TaskQueryRequest {
        if (page == null) page = 0;
        if (size == null) size = 10;
        if (sortBy == null) sortBy = SortField.ID;
        if (direction == null) direction = Sort.Direction.ASC;
    }
}
