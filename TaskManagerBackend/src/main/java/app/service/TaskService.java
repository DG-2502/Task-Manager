package app.service;

import app.domain.*;
import app.exception.AccessDeniedException;
import app.exception.TaskNotFoundException;
import app.exception.UserNotFoundException;
import app.repo.TaskRepository;
import org.springframework.stereotype.Service;
import app.repo.UserRepo;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepo;
    private final UserRepo userRepo;

    public TaskService(TaskRepository taskRepo, UserRepo userRepo) {
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
    }

    public Task createTask(User requester, int userId, String title) throws AccessDeniedException, UserNotFoundException {
        if (!requester.isAdmin() && userId != requester.getId()) {
            throw new AccessDeniedException("Only admins can create tasks for another user");
        }
        User user = userRepo.getById(userId);

        Task task = new Task(title, LocalDate.now(), Task.State.ACTIVE, user.getId());
        return taskRepo.save(task);
    }

    public void closeTask(int id, User requester) throws TaskNotFoundException {
        Task task = taskRepo.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        if (!requester.isAdmin() && task.getCreatorId() != requester.getId()) {
            throw new TaskNotFoundException(id);
        }

        task.setState(Task.State.CLOSED);
        taskRepo.save(task);
    }

    public void deleteTask(User requester, int id) throws TaskNotFoundException {
        Task task = taskRepo.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        if (!requester.isAdmin() && task.getCreatorId() != requester.getId()) {
            throw new TaskNotFoundException(id);
        }

        taskRepo.deleteById(task.getId());
    }

    public void deleteClosed(User requester, int userId) throws AccessDeniedException {
        if (!requester.isAdmin() && userId != requester.getId()) {
            throw new AccessDeniedException("Only admins can delete other's tasks");
        }
        taskRepo.deleteByCreatorIdAndState(userId, Task.State.CLOSED);
    }

    public void updateTask(User requester, int taskId, String newTitle, Task.State state) throws TaskNotFoundException {
        Task task = taskRepo.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));

        if (!requester.isAdmin() && task.getCreatorId() != requester.getId()) {
            throw new TaskNotFoundException(taskId);
        }

        if (newTitle != null && !newTitle.isBlank()) task.setTitle(newTitle);
        if (state != null) task.setState(state);

        taskRepo.save(task);
    }

    public enum TaskFilter {
        ALL, ACTIVE, CLOSED
    }

    public List<Task> getTasks(User requester, TaskFilter filter, int userID) throws AccessDeniedException {
        if (!requester.isAdmin() && userID != requester.getId()) {
            throw new AccessDeniedException("Only admins can get others' tasks");
        }

        return switch (filter) {
            case ALL -> taskRepo.findByCreatorId(userID);
            case ACTIVE -> taskRepo.findByCreatorIdAndState(userID, Task.State.ACTIVE);
            case CLOSED -> taskRepo.findByCreatorIdAndState(userID, Task.State.CLOSED);
        };
    }

    public void deleteTasksByUserId(int id, User requester) throws AccessDeniedException {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only admins can do that");
        }

        taskRepo.deleteByCreatorId(id);
    }

    public Task getById(int id, User requester) throws TaskNotFoundException{
        Task task = taskRepo.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        if (!requester.isAdmin() && task.getCreatorId() != requester.getId()) {
            throw new TaskNotFoundException(id);
        }

        return task;
    }
}
