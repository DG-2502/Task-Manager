package app.web.api;

import app.domain.User;
import app.exception.ValidationException;
import app.service.JwtService;
import app.service.UserService;
import app.web.dto.request.user.ChangePasswordRequest;
import app.web.dto.request.user.RegisterRequest;
import app.web.dto.request.user.UpdateProfileRequest;
import app.web.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserApiController {
    private final UserService userService;
    private final JwtService jwtService;

    public UserApiController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        User user = userService.login(body.get("username"), body.get("password"));
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(Map.of("token", token, "user", UserResponse.from(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logged out");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest body) {
        String password = body.password();
        String passwordConfirm = body.passwordConfirm();
        if (password == null || !password.equals(passwordConfirm)) {
            throw new ValidationException("passwordConfirm", "Passwords do not match");
        }

        userService.register(
                body.username(),
                password,
                User.Status.USER);
        return ResponseEntity.ok("Registered");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<?> update(@AuthenticationPrincipal User me, @Valid @RequestBody UpdateProfileRequest body) {
        userService.update(me, me.getId(), body.username(), null);
        return ResponseEntity.ok("Updated profile");
    }

    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal User me, @Valid @RequestBody ChangePasswordRequest body) {
        userService.changePassword(me, me.getId(), body.currentPassword(), body.newPassword());

        return ResponseEntity.ok("Password changed successfully");
    }
}
