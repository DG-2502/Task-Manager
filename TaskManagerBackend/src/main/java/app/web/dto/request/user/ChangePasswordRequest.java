package app.web.dto.request.user;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {
}
