package app.web.api;

import app.domain.Task;
import app.domain.User;
import app.service.TaskService;
import app.service.UserService;
import app.web.RequestContext;
import app.web.dto.PageResponse;
import app.web.dto.TaskResponse;
import app.web.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
                                               @RequestParam(defaultValue = "ALL") String filter,
                                               @RequestParam(required = false) String search,
                                               HttpServletRequest req) {
        User admin = RequestContext.currentUser(req);
        UserService.UserFilter userFilter = UserService.UserFilter.valueOf(filter.toUpperCase());
        Page<User> users = userService.getUsers(admin, userFilter, page, size, search);
        return PageResponse.from(users, UserResponse::from);
    }

    @GetMapping("/users/{id}/tasks")
    public PageResponse<TaskResponse> getTasks(@PathVariable int id,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(defaultValue = "ALL") String filter,
                                               @RequestParam(required = false) String search,
                                               HttpServletRequest request) {
        User admin = RequestContext.currentUser(request);
        TaskService.TaskFilter taskFilter = TaskService.TaskFilter.valueOf(filter.toUpperCase());
        Page<Task> taskPage = taskService.getTasks(admin, taskFilter, id, page, size, search);
        return PageResponse.from(taskPage, TaskResponse::from);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id, HttpServletRequest req) {
        User admin = RequestContext.currentUser(req);
        userService.deleteUserById(id, admin);
        return ResponseEntity.ok("Deleted user with id: " + id);
    }

    @PostMapping("/users/{userId}/tasks")
    public TaskResponse createTask(@PathVariable int userId, HttpServletRequest req, @RequestBody Map<String, String> body) {
        User admin = RequestContext.currentUser(req);
        Task task = taskService.createTask(admin, userId, body.get("title"));
        return TaskResponse.from(task);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(HttpServletRequest req, @RequestBody Map<String, String> body) {
        User admin = RequestContext.currentUser(req);
        String username = body.get("username");
        String password = body.get("password");
        User.Status status = User.Status.valueOf(body.get("status").toUpperCase());

        userService.createNewUser(admin, username, password, status);
        return ResponseEntity.ok("Created new user");
    }
}
