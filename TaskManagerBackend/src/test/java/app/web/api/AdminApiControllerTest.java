package app.web.api;

import app.domain.Task;
import app.domain.User;
import app.exception.AccessDeniedException;
import app.exception.UserNotFoundException;
import app.exception.ValidationException;
import app.repo.UserRepository;
import app.service.JwtService;
import app.service.TaskService;
import app.service.UserService;
import app.web.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminApiController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepo;

    private User testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin = new User("adminUser", User.Status.ADMIN, "hashed_password");
        testAdmin.setId(99);
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
    void getUsers_ShouldReturn200_WhenAdmin() throws Exception {
        when(userService.getUsers(any(User.class), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/users")
                        .with(authenticatedUser(testAdmin)))
                .andExpect(status().isOk());
    }

    @Test
    void getUsers_ShouldReturn400_WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/admin/users?page=-1")
                        .with(authenticatedUser(testAdmin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("page"))
                .andExpect(jsonPath("$.message").value("Page cannot be negative"));
    }

    @Test
    void getUsers_ShouldReturn400_WhenStatusEnumIsInvalid() throws Exception {
        mockMvc.perform(get("/api/admin/users?status=GOD_MODE")
                        .with(authenticatedUser(testAdmin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("status"))
                .andExpect(jsonPath("$.message").value("Invalid value: GOD_MODE for parameter: status"));
    }

    @Test
    void getTasks_ShouldReturn200_WhenAdmin() throws Exception {
        when(taskService.getTasks(any(User.class), anyInt(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/users/1/tasks")
                        .with(authenticatedUser(testAdmin)))
                .andExpect(status().isOk());

        verify(taskService).getTasks(eq(testAdmin), eq(1), any());
    }

    @Test
    void getTasks_ShouldReturn400_WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/admin/users/1/tasks?page=-1")
                        .with(authenticatedUser(testAdmin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("page"));
    }

    @Test
    void deleteUser_ShouldReturn200_WhenValid() throws Exception {
        mockMvc.perform(delete("/api/admin/users/5")
                        .with(authenticatedUser(testAdmin)))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted user with userId: 5"));

        verify(userService).deleteUserById(eq(5), eq(testAdmin));
    }

    @Test
    void deleteUser_ShouldReturn403_WhenServiceThrowsAccessDenied() throws Exception {
        doThrow(new AccessDeniedException("Admins cannot delete themselves"))
                .when(userService).deleteUserById(eq(99), any());

        mockMvc.perform(delete("/api/admin/users/99")
                        .with(authenticatedUser(testAdmin)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admins cannot delete themselves"));
    }

    @Test
    void createTask_ShouldReturn200_WhenValid() throws Exception {
        String jsonBody = """
                {
                    "title": "Admin Created Task",
                    "priority": "HIGH"
                }
                """;

        Task mockTask = new Task("Admin Created Task", LocalDate.now(), Task.State.ACTIVE, 5, Task.Priority.HIGH);
        mockTask.setId(10);

        when(taskService.createTask(any(User.class), eq(5), eq("Admin Created Task"), eq(Task.Priority.HIGH)))
                .thenReturn(mockTask);

        mockMvc.perform(post("/api/admin/users/5/tasks")
                        .with(authenticatedUser(testAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Admin Created Task"))
                .andExpect(jsonPath("$.creatorId").value(5));
    }

    @Test
    void createTask_ShouldReturn400_WhenTitleIsBlank() throws Exception {
        String jsonBody = """
                {
                    "title": "   "
                }
                """;

        mockMvc.perform(post("/api/admin/users/5/tasks")
                        .with(authenticatedUser(testAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("title"))
                .andExpect(jsonPath("$.message").value("Title cannot be blank or empty"));

        verify(taskService, never()).createTask(any(), anyInt(), anyString(), any());
    }

    @Test
    void createUser_ShouldReturn200_WhenValid() throws Exception {
        String jsonBody = """
                {
                    "username": "newUser",
                    "password": "validPassword",
                    "status": "ADMIN"
                }
                """;

        User mockCreatedUser = new User("newUser", User.Status.ADMIN, "hashed_password");
        mockCreatedUser.setId(20);

        when(userService.createNewUser(any(User.class), eq("newUser"), eq("validPassword"), eq(User.Status.ADMIN)))
                .thenReturn(mockCreatedUser);

        mockMvc.perform(post("/api/admin/users")
                        .with(authenticatedUser(testAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.status").value("ADMIN"));
    }

    @Test
    void createUser_ShouldReturn400_WhenPasswordTooShort() throws Exception {
        String jsonBody = """
                {
                    "username": "newUser",
                    "password": "12"
                }
                """;

        mockMvc.perform(post("/api/admin/users")
                        .with(authenticatedUser(testAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("password"))
                .andExpect(jsonPath("$.message").value("Password should be at least 3 characters long"));
    }

    @Test
    void createUser_ShouldReturn400_WhenUsernameTaken() throws Exception {
        String jsonBody = """
                {
                    "username": "takenUser",
                    "password": "validPassword"
                }
                """;

        doThrow(new ValidationException("username", "Username: takenUser is already taken"))
                .when(userService).createNewUser(any(), eq("takenUser"), any(), any());

        mockMvc.perform(post("/api/admin/users")
                        .with(authenticatedUser(testAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("username"))
                .andExpect(jsonPath("$.message").value("Username: takenUser is already taken"));
    }

    @Test
    void updateUser_ShouldReturn200_WhenOnlyProfileInfoUpdated() throws Exception {
        String jsonBody = """
                {
                    "username": "newUsername",
                    "status": "ADMIN"
                }
                """;

        mockMvc.perform(patch("/api/admin/users/5")
                        .with(authenticatedUser(testAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        verify(userService).update(eq(testAdmin), eq(5), eq("newUsername"), eq(User.Status.ADMIN));
        verify(userService, never()).changePassword(any(), anyInt(), any(), any());
    }

    @Test
    void updateUser_ShouldReturn200_WhenProfileAndPasswordUpdated() throws Exception {
        String jsonBody = """
                {
                    "username": "newUsername",
                    "password": "newAdminPassword"
                }
                """;

        mockMvc.perform(patch("/api/admin/users/5")
                        .with(authenticatedUser(testAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        verify(userService).update(eq(testAdmin), eq(5), eq("newUsername"), isNull());
        verify(userService).changePassword(eq(testAdmin), eq(5), isNull(), eq("newAdminPassword"));
    }

    @Test
    void updateUser_ShouldReturn400_WhenNewPasswordIsTooShort() throws Exception {
        String jsonBody = """
                {
                    "password": "12"
                }
                """;

        mockMvc.perform(patch("/api/admin/users/5")
                        .with(authenticatedUser(testAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("password"));

        verify(userService, never()).changePassword(any(), anyInt(), any(), any());
    }

    @Test
    void updateUser_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        String jsonBody = """
                {
                    "username": "newUsername"
                }
                """;

        doThrow(new UserNotFoundException(5))
                .when(userService).update(any(), eq(5), anyString(), any());

        mockMvc.perform(patch("/api/admin/users/5")
                        .with(authenticatedUser(testAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with ID: 5 was not found!"));
    }
}