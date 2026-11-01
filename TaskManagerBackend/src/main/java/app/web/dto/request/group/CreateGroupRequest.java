package app.web.dto.request.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title is too long")
        String title
) {
}