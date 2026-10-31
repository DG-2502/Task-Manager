package app.service;

import app.domain.Task;
import app.domain.User;
import app.exception.AccessDeniedException;
import app.exception.TaskNotFoundException;
import app.exception.UserNotFoundException;
import app.exception.ValidationException;
import app.repo.TaskRepository;
import app.repo.TaskSpecifications;
import app.repo.UserRepository;
import app.web.dto.request.task.TaskQueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
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

        if (newTitle != null) {
            task.setTitle(newTitle);
        }
        if (state != null) task.setState(state);
        if (priority != null) task.setPriority(priority);

        taskRepo.save(task);
    }

    public Page<Task> getTasks(User requester, int userID, TaskQueryRequest query) throws AccessDeniedException {
        if (!requester.isAdmin() && userID != requester.getId()) {
            throw new AccessDeniedException("Only admins can get others' tasks");
        }

        Sort.Direction direction = query.direction();

        Sort sort = switch (query.sortBy()) {
            case TITLE, PRIORITY -> Sort.unsorted();
            case STARTDATE -> Sort.by(direction, "startDate").and(Sort.by(Sort.Direction.ASC, "id"));
            case ID -> Sort.by(direction, "id");
        };

        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);

        Specification<Task> spec = Specification.where(TaskSpecifications.byCreatorId(userID));

        if (query.state() != null) {
            spec = spec.and(TaskSpecifications.byState(query.state()));
        }
        if (query.search() != null && !query.search().isBlank()) {
            spec = spec.and(TaskSpecifications.titleContains(query.search()));
        }
        if (query.priority() != null) {
            spec = spec.and(TaskSpecifications.byPriority(query.priority()));
        }

        switch (query.sortBy()) {
            case PRIORITY -> spec = spec.and(TaskSpecifications.sortByPriority(direction.isAscending()));
            case TITLE -> spec = spec.and(TaskSpecifications.sortByTitleLower(direction.isAscending()));
        }

        return taskRepo.findAll(spec, pageable);
    }

    @Transactional
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
