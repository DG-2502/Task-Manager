package app.service;

import app.domain.Group;
import app.domain.Task;
import app.domain.User;
import app.exception.*;
import app.repo.GroupRepository;
import app.repo.TaskRepository;
import app.repo.TaskSpecifications;
import app.repo.UserRepository;
import app.web.dto.request.task.CreateTaskRequest;
import app.web.dto.request.task.TaskQueryRequest;
import app.web.dto.request.task.UpdateTaskRequest;
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
    private final GroupRepository groupRepo;

    public TaskService(TaskRepository taskRepo, UserRepository userRepo, GroupRepository groupRepo) {
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
    }

    public Task createTask(User requester, int userId, CreateTaskRequest body) throws AccessDeniedException, UserNotFoundException, ValidationException {
        verifyAdminAccess(requester, userId, "Only admins can create tasks for another user");

        Integer groupId = body.groupId();
        if (groupId != null) {
            verifyGroupOwnership(requester, groupId);
        }

        User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Task task = new Task(body.title(), LocalDate.now(), Task.State.ACTIVE, user.getId(), body.priority(), groupId);
        return taskRepo.save(task);
    }

    public void closeTask(User requester, int taskId) throws TaskNotFoundException {
        Task task = getTaskOrThrowNotFound(requester, taskId);

        task.setState(Task.State.CLOSED);
        taskRepo.save(task);
    }

    public void reopenTask(User requester, int taskId) throws TaskNotFoundException {
        Task task = getTaskOrThrowNotFound(requester, taskId);

        task.setState(Task.State.ACTIVE);
        taskRepo.save(task);
    }

    public void deleteTask(User requester, int id) throws TaskNotFoundException {
        Task task = getTaskOrThrowNotFound(requester, id);

        taskRepo.deleteById(task.getId());
    }

    @Transactional
    public void deleteClosed(User requester, int userId) throws AccessDeniedException {
        verifyAdminAccess(requester, userId, "Only admins can delete others' tasks");

        taskRepo.deleteByCreatorIdAndState(userId, Task.State.CLOSED);
    }

    public void updateTask(User requester, int taskId, UpdateTaskRequest body) throws TaskNotFoundException {
        Task task = getTaskOrThrowNotFound(requester, taskId);

        if (body.title() != null) task.setTitle(body.title());
        if (body.priority() != null) task.setPriority(body.priority());

        taskRepo.save(task);
    }

    public Page<Task> getTasks(User requester, int userId, TaskQueryRequest query) throws AccessDeniedException {
        verifyAdminAccess(requester, userId, "Only admins can get others' tasks");

        Sort.Direction direction = query.direction();

        Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.unsorted());

        Specification<Task> spec = Specification.where(TaskSpecifications.byCreatorId(userId));

        if (query.state() != null) {
            spec = spec.and(TaskSpecifications.byState(query.state()));
        }
        if (query.search() != null && !query.search().isBlank()) {
            spec = spec.and(TaskSpecifications.titleContains(query.search()));
        }
        if (query.priority() != null) {
            spec = spec.and(TaskSpecifications.byPriority(query.priority()));
        }
        if (query.groupId() != null) {
            spec = spec.and(TaskSpecifications.byGroupId(query.groupId()));
        } else if (Boolean.TRUE.equals(query.ungrouped())) {
            spec = spec.and(TaskSpecifications.byUngrouped());
        } else if (Boolean.FALSE.equals(query.ungrouped())) {
            spec = spec.and(TaskSpecifications.byGrouped());
        }

        switch (query.sortBy()) {
            case PRIORITY -> spec = spec.and(TaskSpecifications.sortByPriority(direction.isAscending()));
            case TITLE -> spec = spec.and(TaskSpecifications.sortByTitleLower(direction.isAscending()));
            case STARTDATE -> spec = spec.and(TaskSpecifications.sortByStartDate(direction.isAscending()));
            case ID -> spec = spec.and(TaskSpecifications.sortById(direction.isAscending()));
        }

        return taskRepo.findAll(spec, pageable);
    }

    @Transactional
    public void deleteTasksByUserId(int id, User requester) throws AccessDeniedException {
        verifyAdminAccess(requester, -1, "Only admins can delete others' tasks");

        taskRepo.deleteByCreatorId(id);
    }

    public void setGroup(User requester, int taskId, int groupId) throws TaskNotFoundException, GroupNotFoundException {
        Task task = getTaskOrThrowNotFound(requester, taskId);
        verifyGroupOwnership(requester, groupId);

        task.setGroupId(groupId);
        taskRepo.save(task);
    }

    public void ungroupTask(User requester, int taskId) throws TaskNotFoundException {
        Task task = getTaskOrThrowNotFound(requester, taskId);

        task.setGroupId(null);
        taskRepo.save(task);
    }

    private Task getTaskOrThrowNotFound(User requester, int taskId) throws TaskNotFoundException {
        Task task = taskRepo.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));

        if (!requester.isAdmin() && task.getCreatorId() != requester.getId()) {
            throw new TaskNotFoundException(taskId);
        }

        return task;
    }

    private void verifyGroupOwnership(User requester, Integer groupId) throws GroupNotFoundException {
        Group group = groupRepo.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
        if (!requester.isAdmin() && group.getCreatorId() != requester.getId()) {
            throw new GroupNotFoundException(groupId);
        }
    }

    private void verifyAdminAccess(User requester, int userId, String message) throws AccessDeniedException {
        if (!requester.isAdmin() && userId != requester.getId()) {
            throw new AccessDeniedException(message);
        }
    }
}
