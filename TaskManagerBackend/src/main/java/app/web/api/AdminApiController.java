package app.web.api;

import app.domain.Task;
import app.domain.User;
import app.service.TaskService;
import app.service.UserService;
import app.web.dto.request.task.CreateTaskRequest;
import app.web.dto.request.task.TaskQueryRequest;
import app.web.dto.request.user.CreateUserRequest;
import app.web.dto.request.user.UpdateUserRequest;
import app.web.dto.request.user.UserQueryRequest;
import app.web.dto.response.PageResponse;
import app.web.dto.response.TaskResponse;
import app.web.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {
    private final TaskService taskService;
    private final UserService userService;

    public AdminApiController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/users")
    public PageResponse<UserResponse> getUsers(@Valid @ModelAttribute UserQueryRequest query,
                                               @AuthenticationPrincipal User admin) {
        Page<User> users = userService.getUsers(admin, query);
        return PageResponse.from(users, UserResponse::from);
    }

    @GetMapping("/users/{id}/tasks")
    public PageResponse<TaskResponse> getTasks(@PathVariable int id,
                                               @Valid @ModelAttribute TaskQueryRequest query,
                                               @AuthenticationPrincipal User admin) {
        Page<Task> taskPage = taskService.getTasks(admin, id, query);
        return PageResponse.from(taskPage, TaskResponse::from);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId, @AuthenticationPrincipal User admin) {
        userService.deleteUserById(userId, admin);
        return ResponseEntity.ok("Deleted user with userId: " + userId);
    }

    @PostMapping("/users/{userId}/tasks")
    public TaskResponse createTask(@PathVariable int userId, @AuthenticationPrincipal User admin, @Valid @RequestBody CreateTaskRequest body) {
        Task task = taskService.createTask(admin, userId, body.title(), body.priority());
        return TaskResponse.from(task);
    }

    @PostMapping("/users")
    public UserResponse createUser(@AuthenticationPrincipal User admin, @Valid @RequestBody CreateUserRequest body) {
        User user = userService.createNewUser(admin, body.username(), body.password(), body.status());
        return UserResponse.from(user);
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable int userId, @AuthenticationPrincipal User admin, @Valid @RequestBody UpdateUserRequest body) {
        userService.update(admin, userId, body.username(), body.status());

        if (body.password() != null && !body.password().isBlank()) {
            userService.changePassword(admin, userId, null, body.password());
        }

        return ResponseEntity.ok("Updated user with id: " + userId);
    }
}
