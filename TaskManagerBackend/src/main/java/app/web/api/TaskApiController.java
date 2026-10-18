package app.web.api;

import app.domain.Task;
import app.domain.User;
import app.service.TaskService;
import app.web.RequestContext;
import app.web.dto.CreateTaskRequest;
import app.web.dto.PageResponse;
import app.web.dto.TaskResponse;
import app.web.dto.UpdateTaskRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
                                              @RequestParam(defaultValue = "ALL") String filter,
                                              @RequestParam(required = false) String search,
                                              @RequestParam(defaultValue = "ALL") String priority,
                                              HttpServletRequest request) {
        User me = RequestContext.currentUser(request);

        TaskService.TaskFilter taskFilter = TaskService.TaskFilter.valueOf(filter.toUpperCase());
        TaskService.PriorityFilter priorityFilter = TaskService.PriorityFilter.valueOf(priority.toUpperCase());

        Page<Task> taskPage = taskService.getTasks(me, taskFilter, priorityFilter, me.getId(), page, size, search);
        return PageResponse.from(taskPage, TaskResponse::from);
    }

    @PostMapping("")
    public TaskResponse createTask(HttpServletRequest req, @RequestBody CreateTaskRequest body) {
        User me = RequestContext.currentUser(req);

        Task task = taskService.createTask(me, me.getId(), body.title(), body.priority());
        return TaskResponse.from(task);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> closeTask(@PathVariable int id, HttpServletRequest req) {
        User me = RequestContext.currentUser(req);
        taskService.closeTask(id, me);
        return ResponseEntity.ok("Closed task with id: " + id);
    }

    @PostMapping("/{id}/open")
    public ResponseEntity<?> reopenTask(@PathVariable int id, HttpServletRequest req) {
        User me = RequestContext.currentUser(req);
        taskService.updateTask(me, id, null, Task.State.ACTIVE, null);
        return ResponseEntity.ok("Opened task with id: " + id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable int id, HttpServletRequest req) {
        User me = RequestContext.currentUser(req);
        taskService.deleteTask(me, id);
        return ResponseEntity.ok("Deleted task with id: " + id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable int id, HttpServletRequest req, @RequestBody UpdateTaskRequest body) {
        User me = RequestContext.currentUser(req);

        taskService.updateTask(me, id, body.title(), body.state(), body.priority());
        return ResponseEntity.ok("Updated task with id: " + id);
    }
}
