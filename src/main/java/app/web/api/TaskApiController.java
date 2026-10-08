package app.web.api;

import app.domain.Task;
import app.domain.User;
import app.service.TaskService;
import app.web.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/tasks")
public class TaskApiController {
    private final TaskService taskService;

    public TaskApiController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public List<Task> myTasks(HttpServletRequest request) {
        User me = RequestContext.currentUser(request);
        return taskService.getTasks(me, TaskService.TaskFilter.ALL, me.getId());
    }
}
