package app.web.api;

import app.domain.Task;
import app.domain.User;
import app.service.TaskService;
import app.web.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/tasks")
public class TaskApiController {
    private final TaskService taskService;

    public TaskApiController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/")
    public List<Task> myTasks(HttpServletRequest request) {
        User me = RequestContext.currentUser(request);
        return taskService.getTasks(me, TaskService.TaskFilter.ALL, me.getId());
    }

    @PostMapping("/")
    public Task createTask(HttpServletRequest req, @RequestBody Map<String, String> body) {
        User me = RequestContext.currentUser(req);
        return taskService.createTask(me, me.getId(), body.get("title"));
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
        taskService.updateTask(me, id, null, Task.State.ACTIVE);
        return ResponseEntity.ok("Opened task with id: " + id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable int id, HttpServletRequest req) {
        User me = RequestContext.currentUser(req);
        taskService.deleteTask(me, id);
        return ResponseEntity.ok("Deleted task with id: " + id);
    }
}
