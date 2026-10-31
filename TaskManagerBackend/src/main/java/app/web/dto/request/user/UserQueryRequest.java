package app.web.dto.request.user;

import app.domain.User;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UserQueryRequest(
        @Min(value = 0, message = "Page cannot be negative")
        Integer page,
        @Min(value = 1, message = "Size must be at least 1")
        @Max(value = 100, message = "Size cannot exceed 100")
        Integer size,

        User.Status status,
        @Size(max = 255, message = "String is too long")
        String search
) {
    public UserQueryRequest {
        if (page == null) page = 0;
        if (size == null) size = 10;
    }
}
