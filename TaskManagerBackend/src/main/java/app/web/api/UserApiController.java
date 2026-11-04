package app.web.api;

import app.domain.User;
import app.exception.ValidationException;
import app.service.JwtService;
import app.service.UserService;
import app.web.dto.request.user.ChangePasswordRequest;
import app.web.dto.request.user.LoginRequest;
import app.web.dto.request.user.RegisterRequest;
import app.web.dto.request.user.UpdateProfileRequest;
import app.web.dto.response.LoginResponse;
import app.web.dto.response.MessageResponse;
import app.web.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Users", description = "Endpoints for authentication and profile management")
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserApiController {
    private final UserService userService;
    private final JwtService jwtService;

    public UserApiController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, returns JWT"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest body) {
        User user = userService.login(body);
        String token = jwtService.generateToken(user);

        return new LoginResponse(token, UserResponse.from(user));
    }

    @Operation(summary = "Logout", description = "Client-side logout. The frontend should discard the JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    @PostMapping("/logout")
    public MessageResponse logout() {
        return new MessageResponse("Logged out");
    }

    @Operation(summary = "Register a new user", description = "Creates a standard USER account.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registered successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    })
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest body) {
        String password = body.password();
        String passwordConfirm = body.passwordConfirm();
        if (password == null || !password.equals(passwordConfirm)) {
            throw new ValidationException("passwordConfirm", "Passwords do not match");
        }

        userService.register(
                body.username(),
                password,
                User.Status.USER);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Registered"));
    }

    @Operation(summary = "Get my profile", description = "Returns the authenticated user's profile details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User user) {
        return UserResponse.from(user);
    }

    @Operation(summary = "Update my profile", description = "Allows the authenticated user to update their username.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    @PatchMapping("/me")
    public MessageResponse update(@AuthenticationPrincipal User me, @Valid @RequestBody UpdateProfileRequest body) {
        userService.update(me, me.getId(), body.username(), null);
        return new MessageResponse("Updated profile");
    }

    @Operation(summary = "Change my password", description = "Allows the authenticated user to change their password by providing their current one.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    @PatchMapping("/password")
    public MessageResponse changePassword(@AuthenticationPrincipal User me, @Valid @RequestBody ChangePasswordRequest body) {
        userService.changePassword(me, me.getId(), body);

        return new MessageResponse("Password changed successfully");
    }
}