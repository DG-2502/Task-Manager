package app.service;

import app.domain.*;
import app.exception.AccessDeniedException;
import app.exception.TaskNotFoundException;
import app.exception.UserNotFoundException;
import app.repo.TaskRepository;
import app.repo.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TaskService {
    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    public TaskService(TaskRepository taskRepo, UserRepository userRepo) {
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
    }

    public Task createTask(User requester, int userId, String title) throws AccessDeniedException, UserNotFoundException {
        if (!requester.isAdmin() && userId != requester.getId()) {
            throw new AccessDeniedException("Only admins can create tasks for another user");
        }
        User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

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

    public Page<Task> getTasks(User requester, TaskFilter filter, int userID, int page, int size) throws AccessDeniedException {
        if (!requester.isAdmin() && userID != requester.getId()) {
            throw new AccessDeniedException("Only admins can get others' tasks");
        }

        if (size <= 0) {
            size = 10;
        } else if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        return switch (filter) {
            case ALL -> taskRepo.findByCreatorId(userID, pageable);
            case ACTIVE -> taskRepo.findByCreatorIdAndState(userID, Task.State.ACTIVE, pageable);
            case CLOSED -> taskRepo.findByCreatorIdAndState(userID, Task.State.CLOSED, pageable);
        };
    }

    public void deleteTasksByUserId(int id, User requester) throws AccessDeniedException {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only admins can do that");
        }

        taskRepo.deleteByCreatorId(id);
    }

    public Task getById(int id, User requester) throws TaskNotFoundException {
        Task task = taskRepo.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        if (!requester.isAdmin() && task.getCreatorId() != requester.getId()) {
            throw new TaskNotFoundException(id);
        }

        return task;
    }
}
