package app.web.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username cannot be blank or empty")
        @Size(max = 255, message = "Username is too long")
        @Pattern(regexp = "\\w+", message = "Username can only contain letters, digits and underscore")
        String username,
        @NotBlank(message = "Password is required")
        @Size(min = 3, message = "Password should be at least 3 characters long")
        @Pattern(regexp = "\\w+", message = "Password can only contain letters, digits and underscore")
        String password,
        String passwordConfirm
) {
}
