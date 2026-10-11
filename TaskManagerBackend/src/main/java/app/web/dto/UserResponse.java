package app.web.dto;

import app.domain.User;

public record UserResponse(int id, String username, User.Status status) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getStatus());
    }
}
