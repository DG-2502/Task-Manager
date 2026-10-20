package app.web.dto.request.user;

public record RegisterRequest(
        String username,
        String password,
        String passwordConfirm
) {
}
