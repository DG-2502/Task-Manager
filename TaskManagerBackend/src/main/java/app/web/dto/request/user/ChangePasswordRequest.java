package app.web.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,
        @NotBlank(message = "New password is required")
        @Size(min = 3, message = "Password should be at least 3 characters long")
        @Pattern(regexp = "\\w+", message = "Password can only contain letters, digits and underscore")
        String newPassword
) {
}
