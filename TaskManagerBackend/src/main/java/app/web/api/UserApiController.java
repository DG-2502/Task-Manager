package app.web.api;

import app.domain.User;
import app.exception.ValidationException;
import app.service.UserService;
import app.web.RequestContext;
import app.web.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserApiController {
    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        User user = userService.login(body.get("username"), body.get("password"));
        session.setAttribute("userId", user.getId());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        String passwordConfirm = body.get("passwordConfirm");
        if (password == null || !password.equals(passwordConfirm)) {
            throw new ValidationException("passwordConfirm", "Passwords do not match");
        }

        userService.register(
                body.get("username"),
                body.get("password"),
                User.Status.USER);
        return ResponseEntity.ok("Registered");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        User user = RequestContext.currentUser(request);
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
