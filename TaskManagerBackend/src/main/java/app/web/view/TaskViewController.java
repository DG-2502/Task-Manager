package app.web.view;

import app.domain.Task;
import app.domain.User;
import app.service.TaskService;
import app.web.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class TaskViewController {
    private final TaskService taskService;

    public TaskViewController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public String taskList(HttpServletRequest req, Model model) {
        User me = RequestContext.currentUser(req);
        List<Task> tasks = taskService.getTasks(me, TaskService.TaskFilter.ALL, me.getId());
        model.addAttribute("tasks", tasks);
        model.addAttribute("username", me.getUsername());
        return "tasks";
    }

    @PostMapping("/tasks")
    public String createTask(@RequestParam String title, HttpServletRequest req) {
        User me = RequestContext.currentUser(req);
        taskService.createTask(me, me.getId(), title);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/{id}/close")
    public String closeTask(@PathVariable int id, HttpServletRequest req) {
        User me = RequestContext.currentUser(req);
        taskService.closeTask(id, me);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/{id}/reopen")
    public String reopenTask(@PathVariable int id, HttpServletRequest req) {
        User me = RequestContext.currentUser(req);
        taskService.updateTask(me, id, null, Task.State.ACTIVE);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable int id, HttpServletRequest req) {
        User me = RequestContext.currentUser(req);
        taskService.deleteTask(me, id);
        return "redirect:/tasks";
    }
}