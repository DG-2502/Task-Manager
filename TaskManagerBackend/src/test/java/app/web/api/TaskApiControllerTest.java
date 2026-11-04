package app.web.api;

import app.domain.Task;
import app.domain.User;
import app.exception.GroupNotFoundException;
import app.exception.TaskNotFoundException;
import app.repo.UserRepository;
import app.service.JwtService;
import app.service.TaskService;
import app.web.config.SecurityConfig;
import app.web.dto.request.task.CreateTaskRequest;
import app.web.dto.request.task.TaskQueryRequest;
import app.web.dto.request.task.UpdateTaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskApiController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepo;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testUser", User.Status.USER, "hashed_password");
        testUser.setId(1);
    }

    private static RequestPostProcessor authenticatedUser(User user) {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
            );
            return request;
        };
    }

    @Test
    void createTask_ShouldReturn400_WhenTitleIsEmpty() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("title"))
                .andExpect(jsonPath("$.message").value("Title cannot be blank or empty"));
    }

    @Test
    void createTask_ShouldReturn400_WhenTitleIsTooLong() throws Exception {
        String json = "{\"title\": \"" + "C".repeat(256) + "\"}";
        mockMvc.perform(post("/api/tasks")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("title"))
                .andExpect(jsonPath("$.message").value("Title is too long"));
    }

    @Test
    void createTask_ShouldReturn400_WhenPriorityIsInvalid() throws Exception {
        String json = "{\"title\": \"Valid Title\", \"priority\": \"SUPER_HIGH\"}";
        mockMvc.perform(post("/api/tasks")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request or invalid data type."));
    }

    @Test
    void createTask_ShouldReturn201_AndDefaultToMedium_WhenPriorityIsMissing() throws Exception {
        Task mockTask = new Task("Task without priority", LocalDate.now(), Task.State.ACTIVE, 1, Task.Priority.MEDIUM, null);
        mockTask.setId(1);

        when(taskService.createTask(any(User.class), anyInt(), any(CreateTaskRequest.class)))
                .thenReturn(mockTask);

        mockMvc.perform(post("/api/tasks")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Task without priority\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        ArgumentCaptor<CreateTaskRequest> captor = ArgumentCaptor.forClass(CreateTaskRequest.class);
        verify(taskService).createTask(eq(testUser), eq(1), captor.capture());

        CreateTaskRequest captured = captor.getValue();
        assertEquals("Task without priority", captured.title());
        assertEquals(Task.Priority.MEDIUM, captured.priority());
        assertNull(captured.groupId());
    }

    @Test
    void createTask_ShouldReturn201_WhenGroupIdIsProvided() throws Exception {
        Task mockTask = new Task("Grouped Task", LocalDate.now(), Task.State.ACTIVE, 1, Task.Priority.HIGH, 10);
        mockTask.setId(2);

        when(taskService.createTask(any(User.class), anyInt(), any(CreateTaskRequest.class)))
                .thenReturn(mockTask);

        mockMvc.perform(post("/api/tasks")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Grouped Task",
                                    "priority": "HIGH",
                                    "groupId": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.groupId").value(10));

        ArgumentCaptor<CreateTaskRequest> captor = ArgumentCaptor.forClass(CreateTaskRequest.class);
        verify(taskService).createTask(eq(testUser), eq(1), captor.capture());

        assertEquals(10, captor.getValue().groupId());
    }

    @Test
    void createTask_ShouldReturn404_WhenGroupNotFound() throws Exception {
        when(taskService.createTask(any(), anyInt(), any()))
                .thenThrow(new GroupNotFoundException(999));

        mockMvc.perform(post("/api/tasks")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Task in missing group",
                                    "groupId": 999
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Group with ID: 999 was not found!"));
    }

    @Test
    void updateTask_ShouldReturn400_WhenTitleIsTooLong() throws Exception {
        String json = "{\"title\": \"" + "C".repeat(256) + "\"}";
        mockMvc.perform(patch("/api/tasks/1")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("title"))
                .andExpect(jsonPath("$.message").value("Title is too long"));

        verify(taskService, never()).updateTask(any(), anyInt(), any());
    }

    @Test
    void updateTask_ShouldReturn200_WhenOnlyTitleIsProvided() throws Exception {
        String json = "{\"title\": \"Updated Title\"}";
        mockMvc.perform(patch("/api/tasks/1")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateTaskRequest> captor = ArgumentCaptor.forClass(UpdateTaskRequest.class);
        verify(taskService).updateTask(eq(testUser), eq(1), captor.capture());

        UpdateTaskRequest captured = captor.getValue();
        assertEquals("Updated Title", captured.title());
        assertNull(captured.priority());
    }

    @Test
    void updateTask_ShouldReturn404_WhenTaskNotFound() throws Exception {
        String json = "{\"title\": \"Updated Title\"}";
        doThrow(new TaskNotFoundException(1)).when(taskService).updateTask(any(), anyInt(), any());

        mockMvc.perform(patch("/api/tasks/1")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTasks_ShouldReturn200_AndUseDefaults_WhenNoParamsProvided() throws Exception {
        when(taskService.getTasks(any(), anyInt(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/tasks")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isOk());

        verify(taskService).getTasks(eq(testUser), eq(1), argThat(query ->
                query.page() == 0 && query.size() == 10 &&
                        query.sortBy() == TaskQueryRequest.SortField.ID &&
                        query.direction() == Sort.Direction.ASC &&
                        query.state() == null &&
                        query.priority() == null &&
                        query.groupId() == null &&
                        query.search() == null
        ));
    }

    @Test
    void getTasks_ShouldReturn400_WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/tasks?page=-1")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("page"))
                .andExpect(jsonPath("$.message").value("Page cannot be negative"));
    }

    @Test
    void getTasks_ShouldReturn400_WhenStateEnumIsInvalid() throws Exception {
        mockMvc.perform(get("/api/tasks?state=INVALID_STATE")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("state"))
                .andExpect(jsonPath("$.message").value("Invalid value: INVALID_STATE for parameter: state"));
    }

    @Test
    void getTasks_ShouldReturn200_AndPassGroupIdFilter_WhenGroupIdProvided() throws Exception {
        when(taskService.getTasks(any(), anyInt(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/tasks?groupId=10")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isOk());

        verify(taskService).getTasks(eq(testUser), eq(1), argThat(query ->
                Integer.valueOf(10).equals(query.groupId())
        ));
    }

    @Test
    void getTasks_ShouldReturn400_WhenGroupIdIsZero() throws Exception {
        mockMvc.perform(get("/api/tasks?groupId=0")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("groupId"))
                .andExpect(jsonPath("$.message").value("Group ID must be a positive number"));
    }

    @Test
    void getTasks_ShouldReturn400_WhenGroupIdIsNegative() throws Exception {
        mockMvc.perform(get("/api/tasks?groupId=-1")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("groupId"))
                .andExpect(jsonPath("$.message").value("Group ID must be a positive number"));
    }

    @Test
    void getTasks_ShouldReturn200_WhenUngroupedIsTrue() throws Exception {
        when(taskService.getTasks(any(), anyInt(), any())).thenReturn(Page.empty());
        mockMvc.perform(get("/api/tasks?ungrouped=true").with(authenticatedUser(testUser)))
                .andExpect(status().isOk());
        verify(taskService).getTasks(eq(testUser), eq(1), argThat(q -> Boolean.TRUE.equals(q.ungrouped())));
    }

    @Test
    void getTasks_ShouldReturn200_WhenUngroupedIsFalse() throws Exception {
        when(taskService.getTasks(any(), anyInt(), any())).thenReturn(Page.empty());
        mockMvc.perform(get("/api/tasks?ungrouped=false").with(authenticatedUser(testUser)))
                .andExpect(status().isOk());
        verify(taskService).getTasks(eq(testUser), eq(1), argThat(q -> Boolean.FALSE.equals(q.ungrouped())));
    }

    @Test
    void getTasks_ShouldReturn400_WhenGroupIdAndUngroupedAreCombined() throws Exception {
        mockMvc.perform(get("/api/tasks?groupId=10&ungrouped=true").with(authenticatedUser(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot combine 'groupId' with 'ungrouped=true'"));
    }

    @Test
    void closeTask_ShouldReturn200_WhenValid() throws Exception {
        mockMvc.perform(post("/api/tasks/1/close")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Closed task with id: 1"));

        verify(taskService).closeTask(eq(testUser), eq(1));
    }

    @Test
    void closeTask_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new TaskNotFoundException(1)).when(taskService).closeTask(any(), eq(1));

        mockMvc.perform(post("/api/tasks/1/close")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void openTask_ShouldReturn200_WhenValid() throws Exception {
        mockMvc.perform(post("/api/tasks/1/open")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isOk());

        verify(taskService).reopenTask(eq(testUser), eq(1));
    }

    @Test
    void openTask_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new TaskNotFoundException(1)).when(taskService).reopenTask(any(), eq(1));

        mockMvc.perform(post("/api/tasks/1/open")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_ShouldReturn204_WhenValid() throws Exception {
        mockMvc.perform(delete("/api/tasks/1")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(eq(testUser), eq(1));
    }

    @Test
    void deleteTask_ShouldReturn404_WhenServiceThrowsTaskNotFound() throws Exception {
        doThrow(new TaskNotFoundException(1)).when(taskService).deleteTask(any(), eq(1));

        mockMvc.perform(delete("/api/tasks/1")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task with ID: 1 was not found!"));
    }

    @Test
    void ungroupTask_ShouldReturn200_WhenValid() throws Exception {
        mockMvc.perform(post("/api/tasks/1/ungroup")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ungrouped task with id: 1"));

        verify(taskService).ungroupTask(eq(testUser), eq(1));
    }

    @Test
    void ungroupTask_ShouldReturn404_WhenTaskNotFound() throws Exception {
        doThrow(new TaskNotFoundException(1)).when(taskService).ungroupTask(any(), eq(1));

        mockMvc.perform(post("/api/tasks/1/ungroup")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void setGroup_ShouldReturn200_WhenValid() throws Exception {
        mockMvc.perform(post("/api/tasks/1/group/10")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Set group of task with id: 1 to: 10"));

        verify(taskService).setGroup(eq(testUser), eq(1), eq(10));
    }

    @Test
    void setGroup_ShouldReturn404_WhenTaskNotFound() throws Exception {
        doThrow(new TaskNotFoundException(1)).when(taskService).setGroup(any(), eq(1), eq(10));

        mockMvc.perform(post("/api/tasks/1/group/10")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task with ID: 1 was not found!"));
    }

    @Test
    void setGroup_ShouldReturn404_WhenGroupNotFound() throws Exception {
        doThrow(new GroupNotFoundException(10)).when(taskService).setGroup(any(), eq(1), eq(10));

        mockMvc.perform(post("/api/tasks/1/group/10")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Group with ID: 10 was not found!"));
    }
}