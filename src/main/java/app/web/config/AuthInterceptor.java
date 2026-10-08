package app.web.config;

import app.domain.User;
import app.exception.UserNotFoundException;
import app.repo.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final UserRepo userRepo;


    public AuthInterceptor(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null) return true;

        Integer userId = ((Integer) session.getAttribute("userId"));
        if (userId == null) return true;

        try {
            User user = userRepo.getById(userId);
            request.setAttribute("requester", user);
        } catch (UserNotFoundException e) {
            session.invalidate();
        }
        return true;
    }
}
