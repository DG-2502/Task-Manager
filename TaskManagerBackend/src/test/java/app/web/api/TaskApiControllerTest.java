package app.web.api;

import app.domain.Task;
import app.domain.User;
import app.exception.AccessDeniedException;
import app.exception.TaskNotFoundException;
import app.repo.UserRepository;
import app.service.JwtService;
import app.service.TaskService;
import app.web.dto.request.task.TaskQueryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskApiController.class)
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


    @Test
    @WithMockUser(roles = "USER")
    void createTask_ShouldReturn400_WhenTitleIsEmpty() throws Exception {
        mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content("{\"title\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("title"))
                .andExpect(jsonPath("$.message").value("Title cannot be blank or empty"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createTask_ShouldReturn400_WhenTitleIsTooLong() throws Exception {
        String json = "{\"title\": \"" + "C".repeat(256) + "\"}";
        mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("title"))
                .andExpect(jsonPath("$.message").value("Title is too long"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createTask_ShouldReturn400_WhenPriorityIsInvalid() throws Exception {
        String json = "{\"title\": \"Valid Title\", \"priority\": \"SUPER_HIGH\"}";
        mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request or invalid data type."));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createTask_ShouldReturn200_AndDefaultToMedium_WhenPriorityIsMissing() throws Exception {
        Task mockTask = new Task("Task without priority", LocalDate.now(), Task.State.ACTIVE, 1, Task.Priority.MEDIUM);
        mockTask.setId(1);
        when(taskService.createTask(any(User.class), anyInt(), anyString(), any(Task.Priority.class)))
                .thenReturn(mockTask);

        mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content("{\"title\": \"Task without priority\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        verify(taskService).createTask(any(User.class), anyInt(), eq("Task without priority"), eq(Task.Priority.MEDIUM));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateTask_ShouldReturn400_WhenTitleIsTooLong() throws Exception {
        String json = "{\"title\": \"" + "C".repeat(256) + "\"}";
        mockMvc.perform(patch("/api/tasks/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("title"))
                .andExpect(jsonPath("$.message").value("Title is too long"));

        verify(taskService, never()).updateTask(any(), anyInt(), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateTask_ShouldReturn400_WhenStateIsInvalid() throws Exception {
        String json = "{\"state\": \"SUPER_ACTIVE\"}";
        mockMvc.perform(patch("/api/tasks/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request or invalid data type."));

        verify(taskService, never()).updateTask(any(), anyInt(), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateTask_ShouldReturn200_WhenOnlyTitleIsProvided() throws Exception {
        String json = "{\"title\": \"Updated Title\"}";
        mockMvc.perform(patch("/api/tasks/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());

        verify(taskService).updateTask(any(User.class), eq(1), eq("Updated Title"), isNull(), isNull());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateTask_ShouldReturn404_WhenTaskNotFound() throws Exception {
        String json = "{\"title\": \"Updated Title\"}";
        doThrow(new TaskNotFoundException(1)).when(taskService).updateTask(any(), anyInt(), any(), any(), any());

        mockMvc.perform(patch("/api/tasks/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTasks_ShouldReturn200_AndUseDefaults_WhenNoParamsProvided() throws Exception {
        when(taskService.getTasks(any(), anyInt(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/tasks")).andExpect(status().isOk());

        verify(taskService).getTasks(any(User.class), anyInt(), argThat(query ->
                query.page() == 0 && query.size() == 10 &&
                        query.sortBy() == TaskQueryRequest.SortField.ID &&
                        query.direction() == Sort.Direction.ASC
        ));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTasks_ShouldReturn400_WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/tasks?page=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("page"))
                .andExpect(jsonPath("$.message").value("Page cannot be negative"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTasks_ShouldReturn400_WhenStateEnumIsInvalid() throws Exception {
        mockMvc.perform(get("/api/tasks?state=INVALID_STATE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("state"))
                .andExpect(jsonPath("$.message").value("Invalid value: INVALID_STATE for parameter: state"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void closeTask_ShouldReturn200_WhenValid() throws Exception {
        mockMvc.perform(post("/api/tasks/1/close"))
                .andExpect(status().isOk())
                .andExpect(content().string("Closed task with id: 1"));

        verify(taskService).closeTask(eq(1), any(User.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void closeTask_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new TaskNotFoundException(1)).when(taskService).closeTask(eq(1), any());

        mockMvc.perform(post("/api/tasks/1/close"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void openTask_ShouldReturn200_WhenValid() throws Exception {
        mockMvc.perform(post("/api/tasks/1/open"))
                .andExpect(status().isOk());

        verify(taskService).updateTask(any(User.class), eq(1), isNull(), eq(Task.State.ACTIVE), isNull());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteTask_ShouldReturn200_WhenValid() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted task with id: 1"));

        verify(taskService).deleteTask(any(User.class), eq(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteTask_ShouldReturn403_WhenUnauthorized() throws Exception {
        doThrow(new AccessDeniedException("Unauthorized")).when(taskService).deleteTask(any(), eq(1));

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }
}