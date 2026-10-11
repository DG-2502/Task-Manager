package app.web;

import app.domain.User;
import app.exception.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;

public final class RequestContext {
    private RequestContext() {}

    public static User currentUser(HttpServletRequest request) {
        User user = ((User) request.getAttribute("requester"));
        if (user == null) {
            throw new AuthenticationException("Not authenticated");
        }
        return user;
    }
}
