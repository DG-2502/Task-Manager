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
                                               HttpServletRequest req) {
        User adm = RequestContext.currentUser(req);
        UserService.UserFilter userFilter = UserService.UserFilter.valueOf(filter.toUpperCase());
        Page<User> users = userService.getUsers(adm, userFilter, page, size);
        return PageResponse.from(users, UserResponse::from);
    }

    @GetMapping("/users/{id}/tasks")
    public PageResponse<TaskResponse> getTasks(@PathVariable int id,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(defaultValue = "ALL") String filter,
                                               HttpServletRequest request) {
        User adm = RequestContext.currentUser(request);
        TaskService.TaskFilter taskFilter = TaskService.TaskFilter.valueOf(filter.toUpperCase());
        Page<Task> taskPage = taskService.getTasks(adm, taskFilter, id, page, size);
        return PageResponse.from(taskPage, TaskResponse::from);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id, HttpServletRequest req) {
        User adm = RequestContext.currentUser(req);
        userService.deleteUserById(id, adm);
        return ResponseEntity.ok("Deleted user with id: " + id);
    }

    @PostMapping("/users/{userId}/tasks")
    public TaskResponse createTask(@PathVariable int userId, HttpServletRequest req, @RequestBody Map<String, String> body) {
        User me = RequestContext.currentUser(req);
        Task task = taskService.createTask(me, userId, body.get("title"));
        return TaskResponse.from(task);
    }
}
