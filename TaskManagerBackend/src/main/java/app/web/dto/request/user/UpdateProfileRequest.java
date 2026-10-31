package app.web.dto.request.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 255, message = "Username is too long")
        @Pattern(regexp = "\\w+", message = "Username can only contain letters, digits and underscore")
        String username
) {
}
