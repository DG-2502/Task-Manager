package app.web.api;

import app.domain.User;
import app.exception.AuthenticationException;
import app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/users")
public class UserApiController {
    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session){
        try {
            User user = userService.login(body.get("username"), body.get("password"));
            session.setAttribute("userId", user.getId());
            return ResponseEntity.ok("Logged in as " + user.getUsername());
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session){
        session.invalidate();
        return ResponseEntity.ok("Logged out");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            userService.register(
                    body.get("username"),
                    body.get("password"),
                    User.Status.USER
            );
            return ResponseEntity.ok("Registered");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
