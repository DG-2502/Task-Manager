package app.web.api;

import app.domain.Task;
import app.domain.User;
import app.service.TaskService;
import app.service.UserService;
import app.web.RequestContext;
import app.web.dto.request.user.UpdateUserRequest;
import app.web.dto.request.task.CreateTaskRequest;
import app.web.dto.request.user.CreateUserRequest;
import app.web.dto.response.PageResponse;
import app.web.dto.response.TaskResponse;
import app.web.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
    public PageResponse<UserResponse> getUsers(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(defaultValue = "ALL") String status,
                                               @RequestParam(required = false) String search,
                                               HttpServletRequest req) {
        User admin = RequestContext.currentUser(req);

        UserService.StatusFilter statusFilter = UserService.StatusFilter.valueOf(status.toUpperCase());

        Page<User> users = userService.getUsers(admin, statusFilter, page, size, search);
        return PageResponse.from(users, UserResponse::from);
    }

    @GetMapping("/users/{id}/tasks")
    public PageResponse<TaskResponse> getTasks(@PathVariable int id,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(defaultValue = "ALL") String state,
                                               @RequestParam(required = false) String search,
                                               @RequestParam(defaultValue = "ALL") String priority,
                                               HttpServletRequest request) {
        User admin = RequestContext.currentUser(request);

        TaskService.StateFilter stateFilter = TaskService.StateFilter.valueOf(state.toUpperCase());
        TaskService.PriorityFilter priorityFilter = TaskService.PriorityFilter.valueOf(priority.toUpperCase());

        Page<Task> taskPage = taskService.getTasks(admin, stateFilter, priorityFilter, id, page, size, search);
        return PageResponse.from(taskPage, TaskResponse::from);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId, HttpServletRequest req) {
        User admin = RequestContext.currentUser(req);
        userService.deleteUserById(userId, admin);
        return ResponseEntity.ok("Deleted user with userId: " + userId);
    }

    @PostMapping("/users/{userId}/tasks")
    public TaskResponse createTask(@PathVariable int userId, HttpServletRequest req, @RequestBody CreateTaskRequest body) {
        User admin = RequestContext.currentUser(req);

        Task task = taskService.createTask(admin, userId, body.title(), body.priority());
        return TaskResponse.from(task);
    }

    @PostMapping("/users")
    public UserResponse createUser(HttpServletRequest req, @RequestBody CreateUserRequest body) {
        User admin = RequestContext.currentUser(req);

        User user = userService.createNewUser(admin, body.username(), body.password(), body.status());
        return UserResponse.from(user);
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable int userId, HttpServletRequest req, @RequestBody UpdateUserRequest body) {
        User admin = RequestContext.currentUser(req);

        userService.update(admin, userId, body.username(), body.status());

        if (body.password() != null && !body.password().isBlank()) {
            userService.changePassword(admin, userId, null, body.password());
        }

        return ResponseEntity.ok("Updated user with id: " + userId);
    }
}
