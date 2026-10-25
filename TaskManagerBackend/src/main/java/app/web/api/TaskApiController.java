package app.web.api;

import app.domain.Task;
import app.domain.User;
import app.service.TaskService;
import app.web.dto.request.task.CreateTaskRequest;
import app.web.dto.request.task.UpdateTaskRequest;
import app.web.dto.response.PageResponse;
import app.web.dto.response.TaskResponse;
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
    public PageResponse<TaskResponse> myTasks(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(defaultValue = "ALL") String state,
                                              @RequestParam(required = false) String search,
                                              @RequestParam(defaultValue = "ALL") String priority,
                                              @AuthenticationPrincipal User me) {
        TaskService.StateFilter stateFilter = TaskService.StateFilter.valueOf(state.toUpperCase());
        TaskService.PriorityFilter priorityFilter = TaskService.PriorityFilter.valueOf(priority.toUpperCase());

        Page<Task> taskPage = taskService.getTasks(me, stateFilter, priorityFilter, me.getId(), page, size, search);
        return PageResponse.from(taskPage, TaskResponse::from);
    }

    @PostMapping("")
    public TaskResponse createTask(@AuthenticationPrincipal User me, @RequestBody CreateTaskRequest body) {
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
    public ResponseEntity<?> updateTask(@PathVariable int id, @AuthenticationPrincipal User me, @RequestBody UpdateTaskRequest body) {
        taskService.updateTask(me, id, body.title(), body.state(), body.priority());
        return ResponseEntity.ok("Updated task with id: " + id);
    }
}
