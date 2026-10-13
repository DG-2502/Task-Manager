package app.web.config;

import app.repo.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final UserRepository userRepo;


    public AuthInterceptor(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null) return true;

        Integer userId = ((Integer) session.getAttribute("userId"));
        if (userId == null) return true;

        userRepo.findById(userId).ifPresentOrElse(user -> request.setAttribute("requester", user), session::invalidate);
        return true;
    }
}
