package app.service;

import app.domain.Task;
import app.domain.User;
import app.exception.AccessDeniedException;
import app.exception.TaskNotFoundException;
import app.repo.TaskRepository;
import app.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
