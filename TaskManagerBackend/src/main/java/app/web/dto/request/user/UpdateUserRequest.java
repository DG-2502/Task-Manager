package app.web.dto.request.user;

import app.domain.User;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 255, message = "Username's length cannot exceed 255 characters")
        @Pattern(regexp = "\\w+", message = "Username can only contain letters, digits and underscore")
        String username,
        User.Status status,
        @Size(min = 3, message = "Password should be at least 3 characters long")
        @Pattern(regexp = "\\w+", message = "Password can only contain letters, digits and underscore")
        String password
) {
}
