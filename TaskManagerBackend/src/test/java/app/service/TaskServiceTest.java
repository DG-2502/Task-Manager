package app.service;

import app.domain.Task;
import app.domain.User;
import app.exception.AccessDeniedException;
import app.exception.TaskNotFoundException;
import app.exception.UserNotFoundException;
import app.repo.TaskRepository;
import app.repo.UserRepository;
import app.web.dto.request.task.TaskQueryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private TaskService taskService;

    private User adminUser;
    private User normalUser;
    private Task existingTask;

    @BeforeEach
    void setUp() {
        adminUser = new User("admin", User.Status.ADMIN, "hash");
        adminUser.setId(1);

        normalUser = new User("user", User.Status.USER, "hash");
        normalUser.setId(2);

        existingTask = new Task("title", null, Task.State.ACTIVE, normalUser.getId(), Task.Priority.MEDIUM);
        existingTask.setId(1);
    }

    @Test
    void createTask_ShouldThrow_WhenNormalUserCreatesTaskForSomeoneElse() {
        int targetId = 100;

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
        {
            taskService.createTask(normalUser, targetId, "new task", Task.Priority.MEDIUM);
        });

        assertEquals("Only admins can create tasks for another user", exception.getMessage());

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void createTask_ShouldSaveTask_WhenUserCreatesTaskForThemselves() {
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(taskRepo.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setId(50);
            return savedTask;
        });

        Task result = taskService.createTask(normalUser, normalUser.getId(), "My New Task", Task.Priority.MEDIUM);

        assertNotNull(result);
        assertEquals(50, result.getId());
        assertEquals("My New Task", result.getTitle());
        assertEquals(normalUser.getId(), result.getCreatorId());

        verify(taskRepo, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_ShouldThrowUserNotFound_WhenTargetUserDoesNotExist() {
        when(userRepo.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            taskService.createTask(adminUser, 999, "Task", Task.Priority.HIGH);
        });

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void closeTask_ShouldThrowNotFound_WhenTaskDoesNotExist() {
        when(taskRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.closeTask(999, normalUser);
        });
    }

    @Test
    void closeTask_ShouldThrowNotFound_WhenUserDoesNotOwnTask() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        User attacker = new User("attacker", User.Status.USER, "hash");
        attacker.setId(3);

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.closeTask(existingTask.getId(), attacker);
        });

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void closeTask_ShouldSucceed_WhenAdminClosesAnyoneTask() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        taskService.closeTask(existingTask.getId(), adminUser);

        assertEquals(Task.State.CLOSED, existingTask.getState());
        verify(taskRepo, times(1)).save(existingTask);
    }

    @Test
    void getTasks_ShouldThrowAccessDenied_WhenUserRequestsOthersTasks() {
        TaskQueryRequest query = new TaskQueryRequest(0, 10, null, null, null, null, null);

        assertThrows(AccessDeniedException.class, () -> {
            taskService.getTasks(normalUser, 999, query); // normalUser trying to get user 999's tasks
        });

        verify(taskRepo, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getTasks_ShouldReturnPage_WhenUserRequestsOwnTasks() {
        TaskQueryRequest query = new TaskQueryRequest(0, 10, null, null, null, null, null);
        when(taskRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        Page<Task> result = taskService.getTasks(normalUser, normalUser.getId(), query);

        assertNotNull(result);
        verify(taskRepo).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getById_ShouldThrowNotFound_WhenTaskDoesNotExist() {
        when(taskRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.getById(999, normalUser);
        });
    }

    @Test
    void getById_ShouldThrowNotFound_WhenUserDoesNotOwnTask() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        User attacker = new User("attacker", User.Status.USER, "hash");
        attacker.setId(3);

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.getById(existingTask.getId(), attacker);
        });
    }

    @Test
    void getById_ShouldReturnTask_WhenAdminRequests() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        Task result = taskService.getById(existingTask.getId(), adminUser);
        assertEquals(existingTask, result);
    }

    @Test
    void deleteTask_ShouldThrowNotFound_WhenUserDoesNotOwnTask() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        User attacker = new User("attacker", User.Status.USER, "hash");
        attacker.setId(3);

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.deleteTask(attacker, existingTask.getId());
        });

        verify(taskRepo, never()).deleteById(anyInt());
    }

    @Test
    void deleteTask_ShouldDelete_WhenOwnerDeletes() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        taskService.deleteTask(normalUser, existingTask.getId());

        verify(taskRepo).deleteById(existingTask.getId());
    }

    @Test
    void deleteClosed_ShouldThrowAccessDenied_WhenUserIsNotAdminAndDeletesOthersTasks() {
        assertThrows(AccessDeniedException.class, () -> {
            taskService.deleteClosed(normalUser, 33);
        });
        verify(taskRepo, never()).deleteByCreatorIdAndState(33, Task.State.CLOSED);
    }

    @Test
    void deleteClosed_ShouldCallRepo_WhenAdmin() {
        taskService.deleteClosed(adminUser, 5);
        verify(taskRepo).deleteByCreatorIdAndState(5, Task.State.CLOSED);
    }

    @Test
    void deleteTasksByUserId_ShouldThrowAccessDenied_WhenUserIsNotAdmin() {
        assertThrows(AccessDeniedException.class, () -> {
            taskService.deleteTasksByUserId(5, normalUser);
        });
    }

    @Test
    void deleteTasksByUserId_ShouldCallRepo_WhenAdmin() {
        taskService.deleteTasksByUserId(5, adminUser);
        verify(taskRepo).deleteByCreatorId(5);
    }
}
