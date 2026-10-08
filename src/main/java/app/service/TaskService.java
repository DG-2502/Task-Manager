package app.service;

import app.domain.*;
import app.exception.AccessDeniedException;
import app.exception.TaskNotFoundException;
import app.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import app.repo.TaskRepo;
import app.repo.UserRepo;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepo taskRepo;
    private final UserRepo userRepo;

    public TaskService(TaskRepo taskRepo, UserRepo userRepo) {
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
    }

    public void createTask(User requester, int userId, String title) throws AccessDeniedException, UserNotFoundException {
        if (!requester.isAdmin() && userId != requester.getId()) {
            throw new AccessDeniedException("Only admins can create tasks for another user");
        }
        User user = userRepo.getById(userId);

        taskRepo.add(new Task(title, LocalDate.now(), Task.State.ACTIVE, user.getId()));
    }

    public void closeTask(int id, User requester) throws TaskNotFoundException {
        Task task = taskRepo.getById(id);

        if (!requester.isAdmin() && task.getCreatorId() != requester.getId()) {
            throw new TaskNotFoundException(id);
        }

        task.setState(Task.State.CLOSED);
        taskRepo.update(task);
    }

    public void deleteTask(User requester, int id) throws TaskNotFoundException {
        Task task = taskRepo.getById(id);

        if (!requester.isAdmin() && task.getCreatorId() != requester.getId()) {
            throw new TaskNotFoundException(id);
        }

        taskRepo.delete(task.getId());
    }

    public void deleteClosed(User requester, int userId) throws AccessDeniedException {
        if (!requester.isAdmin() && userId != requester.getId()) {
            throw new AccessDeniedException("Only admins can delete other's tasks");
        }
        taskRepo.deleteByStateAndUser(Task.State.CLOSED, userId);
    }

    public void updateTask(User requester, int taskId, String newTitle, Task.State state) throws TaskNotFoundException {
        Task task = taskRepo.getById(taskId);

        if (!requester.isAdmin() && task.getCreatorId() != requester.getId()) {
            throw new TaskNotFoundException(taskId);
        }

        if (newTitle != null && !newTitle.isBlank()) task.setTitle(newTitle);
        if (state != null) task.setState(state);

        taskRepo.update(task);
    }

    public enum TaskFilter {
        ALL, ACTIVE, CLOSED;
    }

    public List<Task> getTasks(User requester, TaskFilter filter, int userID) throws AccessDeniedException {
        if (!requester.isAdmin() && userID != requester.getId()) {
            throw new AccessDeniedException("Only admins can get others' tasks");
        }

        return switch (filter) {
            case ALL -> taskRepo.getByUser(userID);
            case ACTIVE -> taskRepo.getByStateAndUser(Task.State.ACTIVE, userID);
            case CLOSED -> taskRepo.getByStateAndUser(Task.State.CLOSED, userID);
        };
    }

    public void deleteTasksByUserId(int id, User requester) throws AccessDeniedException {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only admins can do that");
        }

        taskRepo.deleteByUserId(id);
    }

    public Task getById(int id, User requester) throws TaskNotFoundException{
        Task task = taskRepo.getById(id);

        if (!requester.isAdmin() && task.getCreatorId() != requester.getId()) {
            throw new TaskNotFoundException(id);
        }

        return task;
    }
}
