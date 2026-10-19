package app.service;

import app.domain.*;
import app.exception.AccessDeniedException;
import app.exception.TaskNotFoundException;
import app.exception.UserNotFoundException;
import app.exception.ValidationException;
import app.repo.TaskRepository;
import app.repo.TaskSpecifications;
import app.repo.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    public Task createTask(User requester, int userId, String title, Task.Priority priority) throws AccessDeniedException, UserNotFoundException, ValidationException {
        if (!requester.isAdmin() && userId != requester.getId()) {
            throw new AccessDeniedException("Only admins can create tasks for another user");
        }

        validateTitle(title);

        User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Task task = new Task(title, LocalDate.now(), Task.State.ACTIVE, user.getId(), priority);
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

    public void updateTask(User requester, int taskId, String newTitle, Task.State state, Task.Priority priority) throws TaskNotFoundException {
        Task task = taskRepo.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));

        if (!requester.isAdmin() && task.getCreatorId() != requester.getId()) {
            throw new TaskNotFoundException(taskId);
        }

        if (newTitle != null && !newTitle.isBlank()) {
            validateTitle(newTitle);
            task.setTitle(newTitle);
        }
        if (state != null) task.setState(state);
        if (priority != null) task.setPriority(priority);

        taskRepo.save(task);
    }

    public enum StateFilter {
        ALL, ACTIVE, CLOSED
    }

    public enum PriorityFilter {
        ALL, LOW, MEDIUM, HIGH
    }

    public Page<Task> getTasks(User requester, StateFilter stateFilter, PriorityFilter priorityFilter, int userID, int page, int size, String search) throws AccessDeniedException {
        if (!requester.isAdmin() && userID != requester.getId()) {
            throw new AccessDeniedException("Only admins can get others' tasks");
        }

        if (size <= 0) {
            size = 10;
        } else if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Specification<Task> spec = Specification.where(TaskSpecifications.byCreatorId(userID));

        if (stateFilter != StateFilter.ALL) {
            Task.State state = Task.State.valueOf(stateFilter.name());
            spec = spec.and(TaskSpecifications.byState(state));
        }

        if (search != null && !search.isBlank()) spec = spec.and(TaskSpecifications.titleContains(search));

        if (priorityFilter != PriorityFilter.ALL) {
            Task.Priority priority = Task.Priority.valueOf(priorityFilter.name());
            spec = spec.and(TaskSpecifications.byPriority(priority));
        }

        return taskRepo.findAll(spec, pageable);
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

    private void validateTitle(String title) throws ValidationException {
        if (title == null || title.isBlank()) {
            throw new ValidationException("title", "Title must not be empty");
        }

        if (title.length() > 255) {
            throw new ValidationException("title", "Title's length cannot exceed 255 characters");
        }
    }
}
