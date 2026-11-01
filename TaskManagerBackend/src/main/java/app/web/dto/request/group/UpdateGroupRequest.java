package app.web.dto.request.group;

import jakarta.validation.constraints.Size;

public record UpdateGroupRequest(
        @Size(max = 255, message = "Title is too long")
        String title
) {
}
