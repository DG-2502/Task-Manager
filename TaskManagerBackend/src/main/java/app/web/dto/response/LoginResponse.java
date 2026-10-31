package app.web.dto.response;

public record LoginResponse(String token, UserResponse user) {
}