package app.web.api;

import app.domain.Task;
import app.domain.User;
import app.service.TaskService;
import app.service.UserService;
import app.web.dto.request.task.CreateTaskRequest;
import app.web.dto.request.user.CreateUserRequest;
import app.web.dto.request.user.UpdateUserRequest;
import app.web.dto.response.PageResponse;
import app.web.dto.response.TaskResponse;
import app.web.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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
    public PageResponse<UserResponse> getUsers(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(defaultValue = "ALL") String status,
                                               @RequestParam(required = false) String search,
                                               @AuthenticationPrincipal User admin) {
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
                                               @RequestParam(defaultValue = "id") String sortBy,
                                               @RequestParam(defaultValue = "ASC") String direction,
                                               @AuthenticationPrincipal User admin) {
        TaskService.StateFilter stateFilter = TaskService.StateFilter.valueOf(state.toUpperCase());
        TaskService.PriorityFilter priorityFilter = TaskService.PriorityFilter.valueOf(priority.toUpperCase());
        TaskService.SortField sortField = TaskService.SortField.valueOf(sortBy.toUpperCase());
        Sort.Direction direction1 = Sort.Direction.valueOf(direction.toUpperCase());

        Page<Task> taskPage = taskService.getTasks(admin, stateFilter, priorityFilter, id, page, size, search, sortField, direction1);
        return PageResponse.from(taskPage, TaskResponse::from);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId, @AuthenticationPrincipal User admin) {
        userService.deleteUserById(userId, admin);
        return ResponseEntity.ok("Deleted user with userId: " + userId);
    }

    @PostMapping("/users/{userId}/tasks")
    public TaskResponse createTask(@PathVariable int userId, @AuthenticationPrincipal User admin, @RequestBody CreateTaskRequest body) {
        Task task = taskService.createTask(admin, userId, body.title(), body.priority());
        return TaskResponse.from(task);
    }

    @PostMapping("/users")
    public UserResponse createUser(@AuthenticationPrincipal User admin, @RequestBody CreateUserRequest body) {
        User user = userService.createNewUser(admin, body.username(), body.password(), body.status());
        return UserResponse.from(user);
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable int userId, @AuthenticationPrincipal User admin, @RequestBody UpdateUserRequest body) {
        userService.update(admin, userId, body.username(), body.status());

        if (body.password() != null && !body.password().isBlank()) {
            userService.changePassword(admin, userId, null, body.password());
        }

        return ResponseEntity.ok("Updated user with id: " + userId);
    }
}
