package app.web.dto.request.user;

import app.domain.User;

public record CreateUserRequest(
        String username,
        String password,
        User.Status status
) {
    public CreateUserRequest {
        if (status == null) {
            status = User.Status.USER;
        }
    }
}
