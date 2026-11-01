package app.service;

import app.domain.Group;
import app.domain.Task;
import app.domain.User;
import app.exception.AccessDeniedException;
import app.exception.GroupNotFoundException;
import app.exception.TaskNotFoundException;
import app.exception.UserNotFoundException;
import app.repo.GroupRepository;
import app.repo.TaskRepository;
import app.repo.UserRepository;
import app.web.dto.request.task.CreateTaskRequest;
import app.web.dto.request.task.TaskQueryRequest;
import app.web.dto.request.task.UpdateTaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private GroupRepository groupRepo;

    @InjectMocks
    private TaskService taskService;

    private User adminUser;
    private User normalUser;
    private User otherUser;
    private Task existingTask;
    private Group testGroup;

    @BeforeEach
    void setUp() {
        adminUser = new User("admin", User.Status.ADMIN, "hash");
        adminUser.setId(1);

        normalUser = new User("user", User.Status.USER, "hash");
        normalUser.setId(2);

        otherUser = new User("other", User.Status.USER, "hash");
        otherUser.setId(3);

        testGroup = new Group("Test Group", normalUser.getId());
        testGroup.setId(10);

        existingTask = new Task("title", LocalDate.now(), Task.State.ACTIVE, normalUser.getId(),
                Task.Priority.MEDIUM, null);
        existingTask.setId(1);
    }

    @Test
    void createTask_ShouldThrow_WhenNormalUserCreatesTaskForSomeoneElse() {
        CreateTaskRequest request = new CreateTaskRequest("new task", Task.Priority.MEDIUM, null);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                taskService.createTask(normalUser, 100, request));

        assertEquals("Only admins can create tasks for another user", exception.getMessage());
        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void createTask_ShouldSaveTask_WhenUserCreatesTaskForThemselves() {
        CreateTaskRequest request = new CreateTaskRequest("My New Task", Task.Priority.MEDIUM, null);

        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(taskRepo.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setId(50);
            return savedTask;
        });

        Task result = taskService.createTask(normalUser, normalUser.getId(), request);

        assertNotNull(result);
        assertEquals(50, result.getId());
        assertEquals("My New Task", result.getTitle());
        assertEquals(normalUser.getId(), result.getCreatorId());
        assertNull(result.getGroupId());
        assertEquals(Task.State.ACTIVE, result.getState());
        assertEquals(Task.Priority.MEDIUM, result.getPriority());

        verify(taskRepo, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_ShouldThrowUserNotFound_WhenTargetUserDoesNotExist() {
        CreateTaskRequest request = new CreateTaskRequest("Task", Task.Priority.HIGH, null);
        when(userRepo.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                taskService.createTask(adminUser, 999, request));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void createTask_ShouldThrowGroupNotFound_WhenGroupDoesNotExist() {
        CreateTaskRequest request = new CreateTaskRequest("Task", Task.Priority.HIGH, 999);
        when(groupRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(GroupNotFoundException.class, () ->
                taskService.createTask(normalUser, normalUser.getId(), request));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void createTask_ShouldThrowGroupNotFound_WhenUserAssignsTaskToOthersGroup() {
        CreateTaskRequest request = new CreateTaskRequest("Task", Task.Priority.HIGH, 999);

        Group otherUsersGroup = new Group("Other Group", otherUser.getId());
        otherUsersGroup.setId(999);
        when(groupRepo.findById(999)).thenReturn(Optional.of(otherUsersGroup));

        assertThrows(GroupNotFoundException.class, () ->
                taskService.createTask(normalUser, normalUser.getId(), request));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void createTask_ShouldSaveTask_WhenUserAssignsTaskToTheirOwnGroup() {
        CreateTaskRequest request = new CreateTaskRequest("Grouped Task", Task.Priority.HIGH, testGroup.getId());

        when(groupRepo.findById(testGroup.getId())).thenReturn(Optional.of(testGroup));
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(taskRepo.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.createTask(normalUser, normalUser.getId(), request);

        assertEquals(testGroup.getId(), result.getGroupId());
        verify(taskRepo).save(any(Task.class));
    }

    @Test
    void updateTask_ShouldThrowNotFound_WhenUserDoesNotOwnTask() {
        UpdateTaskRequest request = new UpdateTaskRequest("New Title", null);
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        assertThrows(TaskNotFoundException.class, () ->
                taskService.updateTask(otherUser, existingTask.getId(), request));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void updateTask_ShouldThrowNotFound_WhenTaskDoesNotExist() {
        UpdateTaskRequest request = new UpdateTaskRequest("New Title", null);
        when(taskRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () ->
                taskService.updateTask(normalUser, 999, request));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void updateTask_ShouldUpdateFields_WhenValid() {
        UpdateTaskRequest request = new UpdateTaskRequest("Updated Title", Task.Priority.HIGH);
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        taskService.updateTask(normalUser, existingTask.getId(), request);

        assertEquals("Updated Title", existingTask.getTitle());
        assertEquals(Task.State.ACTIVE, existingTask.getState());
        assertEquals(Task.Priority.HIGH, existingTask.getPriority());
        assertNull(existingTask.getGroupId());
        verify(taskRepo).save(existingTask);
    }

    @Test
    void updateTask_ShouldLeaveFieldsUntouched_WhenBothAreNull() {
        UpdateTaskRequest request = new UpdateTaskRequest(null, null);
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        taskService.updateTask(normalUser, existingTask.getId(), request);

        assertEquals("title", existingTask.getTitle());
        assertEquals(Task.Priority.MEDIUM, existingTask.getPriority());
        verify(taskRepo).save(existingTask);
    }

    @Test
    void closeTask_ShouldThrowNotFound_WhenTaskDoesNotExist() {
        when(taskRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () ->
                taskService.closeTask(normalUser, 999));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void closeTask_ShouldThrowNotFound_WhenUserDoesNotOwnTask() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        assertThrows(TaskNotFoundException.class, () ->
                taskService.closeTask(otherUser, existingTask.getId()));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void closeTask_ShouldSucceed_WhenOwnerCloses() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        taskService.closeTask(normalUser, existingTask.getId());

        assertEquals(Task.State.CLOSED, existingTask.getState());
        verify(taskRepo).save(existingTask);
    }

    @Test
    void closeTask_ShouldSucceed_WhenAdminClosesAnyoneTask() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        taskService.closeTask(adminUser, existingTask.getId());

        assertEquals(Task.State.CLOSED, existingTask.getState());
        verify(taskRepo).save(existingTask);
    }

    @Test
    void reopenTask_ShouldThrowNotFound_WhenTaskDoesNotExist() {
        when(taskRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () ->
                taskService.reopenTask(normalUser, 999));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void reopenTask_ShouldThrowNotFound_WhenUserDoesNotOwnTask() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        assertThrows(TaskNotFoundException.class, () ->
                taskService.reopenTask(otherUser, existingTask.getId()));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void reopenTask_ShouldSucceed_WhenOwnerReopens() {
        existingTask.setState(Task.State.CLOSED);
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        taskService.reopenTask(normalUser, existingTask.getId());

        assertEquals(Task.State.ACTIVE, existingTask.getState());
        verify(taskRepo).save(existingTask);
    }

    @Test
    void reopenTask_ShouldSucceed_WhenAdminReopens() {
        existingTask.setState(Task.State.CLOSED);
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        taskService.reopenTask(adminUser, existingTask.getId());

        assertEquals(Task.State.ACTIVE, existingTask.getState());
        verify(taskRepo).save(existingTask);
    }

    @Test
    void deleteTask_ShouldThrowNotFound_WhenTaskDoesNotExist() {
        when(taskRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () ->
                taskService.deleteTask(normalUser, 999));

        verify(taskRepo, never()).deleteById(anyInt());
    }

    @Test
    void deleteTask_ShouldThrowNotFound_WhenUserDoesNotOwnTask() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        assertThrows(TaskNotFoundException.class, () ->
                taskService.deleteTask(otherUser, existingTask.getId()));

        verify(taskRepo, never()).deleteById(anyInt());
    }

    @Test
    void deleteTask_ShouldDelete_WhenOwnerDeletes() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        taskService.deleteTask(normalUser, existingTask.getId());

        verify(taskRepo).deleteById(existingTask.getId());
    }

    @Test
    void deleteTask_ShouldDelete_WhenAdminDeletes() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        taskService.deleteTask(adminUser, existingTask.getId());

        verify(taskRepo).deleteById(existingTask.getId());
    }

    @Test
    void deleteClosed_ShouldThrowAccessDenied_WhenUserIsNotAdminAndDeletesOthersTasks() {
        assertThrows(AccessDeniedException.class, () ->
                taskService.deleteClosed(normalUser, 33));

        verify(taskRepo, never()).deleteByCreatorIdAndState(33, Task.State.CLOSED);
    }

    @Test
    void deleteClosed_ShouldCallRepo_WhenAdmin() {
        taskService.deleteClosed(adminUser, 5);
        verify(taskRepo).deleteByCreatorIdAndState(5, Task.State.CLOSED);
    }

    @Test
    void deleteTasksByUserId_ShouldThrowAccessDenied_WhenUserIsNotAdmin() {
        assertThrows(AccessDeniedException.class, () ->
                taskService.deleteTasksByUserId(5, normalUser));

        verify(taskRepo, never()).deleteByCreatorId(anyInt());
    }

    @Test
    void deleteTasksByUserId_ShouldCallRepo_WhenAdmin() {
        taskService.deleteTasksByUserId(5, adminUser);
        verify(taskRepo).deleteByCreatorId(5);
    }

    @Test
    void setGroup_ShouldThrowTaskNotFound_WhenTaskDoesNotExist() {
        when(taskRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () ->
                taskService.setGroup(normalUser, 999, testGroup.getId()));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void setGroup_ShouldThrowTaskNotFound_WhenNonOwnerTriesToSetGroup() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        assertThrows(TaskNotFoundException.class, () ->
                taskService.setGroup(otherUser, existingTask.getId(), testGroup.getId()));

        verify(groupRepo, never()).findById(anyInt());
        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void setGroup_ShouldThrowGroupNotFound_WhenGroupDoesNotExist() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));
        when(groupRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(GroupNotFoundException.class, () ->
                taskService.setGroup(normalUser, existingTask.getId(), 999));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void setGroup_ShouldThrowGroupNotFound_WhenNonAdminSetsOthersGroup() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        Group othersGroup = new Group("Other", otherUser.getId());
        othersGroup.setId(999);
        when(groupRepo.findById(999)).thenReturn(Optional.of(othersGroup));

        assertThrows(GroupNotFoundException.class, () ->
                taskService.setGroup(normalUser, existingTask.getId(), 999));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void setGroup_ShouldSetGroup_WhenOwnerSetsOwnGroup() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));
        when(groupRepo.findById(testGroup.getId())).thenReturn(Optional.of(testGroup));

        taskService.setGroup(normalUser, existingTask.getId(), testGroup.getId());

        assertEquals(testGroup.getId(), existingTask.getGroupId());
        verify(taskRepo).save(existingTask);
    }

    @Test
    void setGroup_ShouldSetGroup_WhenAdminSetsAnyGroup() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        Group othersGroup = new Group("Other", otherUser.getId());
        othersGroup.setId(999);
        when(groupRepo.findById(999)).thenReturn(Optional.of(othersGroup));

        taskService.setGroup(adminUser, existingTask.getId(), 999);

        assertEquals(999, existingTask.getGroupId());
        verify(taskRepo).save(existingTask);
    }

    @Test
    void ungroupTask_ShouldThrowTaskNotFound_WhenTaskDoesNotExist() {
        when(taskRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () ->
                taskService.ungroupTask(normalUser, 999));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void ungroupTask_ShouldThrowTaskNotFound_WhenNonOwnerTriesToUngroup() {
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        assertThrows(TaskNotFoundException.class, () ->
                taskService.ungroupTask(otherUser, existingTask.getId()));

        verify(taskRepo, never()).save(any(Task.class));
    }

    @Test
    void ungroupTask_ShouldSetGroupIdToNull_WhenOwnerUngroups() {
        existingTask.setGroupId(testGroup.getId());
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        taskService.ungroupTask(normalUser, existingTask.getId());

        assertNull(existingTask.getGroupId());
        verify(taskRepo).save(existingTask);
    }

    @Test
    void ungroupTask_ShouldSucceed_WhenAdminUngroups() {
        existingTask.setGroupId(testGroup.getId());
        when(taskRepo.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        taskService.ungroupTask(adminUser, existingTask.getId());

        assertNull(existingTask.getGroupId());
        verify(taskRepo).save(existingTask);
    }

    @Test
    void getTasks_ShouldThrowAccessDenied_WhenUserRequestsOthersTasks() {
        TaskQueryRequest query = new TaskQueryRequest(0, 10, null, null, null, null, null);

        assertThrows(AccessDeniedException.class, () ->
                taskService.getTasks(normalUser, 999, query));

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
    void getTasks_ShouldNotThrow_WhenAllFiltersAreProvided() {
        TaskQueryRequest query = new TaskQueryRequest(
                0, 10,
                Task.State.ACTIVE,
                Task.Priority.HIGH,
                TaskQueryRequest.SortField.PRIORITY,
                Sort.Direction.ASC,
                "search");
        when(taskRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        assertDoesNotThrow(() -> taskService.getTasks(normalUser, normalUser.getId(), query));
        verify(taskRepo).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getTasks_ShouldNotThrow_WhenSearchIsBlank() {
        TaskQueryRequest query = new TaskQueryRequest(0, 10, null, null, null, null, "   ");
        when(taskRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        assertDoesNotThrow(() -> taskService.getTasks(normalUser, normalUser.getId(), query));
    }
}