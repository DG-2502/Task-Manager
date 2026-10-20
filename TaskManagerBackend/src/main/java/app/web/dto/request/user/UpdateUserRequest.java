package app.web.dto.request.user;

import app.domain.User;

public record UpdateUserRequest(
        String username,
        User.Status status,
        String password
) {
}
