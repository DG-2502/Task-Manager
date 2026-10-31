package app.web.api;

import app.domain.Task;
import app.domain.User;
import app.service.TaskService;
import app.web.dto.request.task.CreateTaskRequest;
import app.web.dto.request.task.TaskQueryRequest;
import app.web.dto.request.task.UpdateTaskRequest;
import app.web.dto.response.PageResponse;
import app.web.dto.response.TaskResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskApiController {
    private final TaskService taskService;

    public TaskApiController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("")
    public PageResponse<TaskResponse> myTasks(@Valid @ModelAttribute TaskQueryRequest query,
                                              @AuthenticationPrincipal User me) {
        Page<Task> taskPage = taskService.getTasks(me, me.getId(), query);
        return PageResponse.from(taskPage, TaskResponse::from);
    }

    @PostMapping("")
    public TaskResponse createTask(@AuthenticationPrincipal User me, @Valid @RequestBody CreateTaskRequest body) {
        Task task = taskService.createTask(me, me.getId(), body.title(), body.priority());
        return TaskResponse.from(task);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> closeTask(@PathVariable int id, @AuthenticationPrincipal User me) {
        taskService.closeTask(id, me);
        return ResponseEntity.ok("Closed task with id: " + id);
    }

    @PostMapping("/{id}/open")
    public ResponseEntity<?> reopenTask(@PathVariable int id, @AuthenticationPrincipal User me) {
        taskService.updateTask(me, id, null, Task.State.ACTIVE, null);
        return ResponseEntity.ok("Opened task with id: " + id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable int id, @AuthenticationPrincipal User me) {
        taskService.deleteTask(me, id);
        return ResponseEntity.ok("Deleted task with id: " + id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable int id, @AuthenticationPrincipal User me, @Valid @RequestBody UpdateTaskRequest body) {
        taskService.updateTask(me, id, body.title(), body.state(), body.priority());
        return ResponseEntity.ok("Updated task with id: " + id);
    }
}
