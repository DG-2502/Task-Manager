package app.web.api;

import app.domain.User;
import app.exception.AuthenticationException;
import app.repo.UserRepository;
import app.service.JwtService;
import app.service.UserService;
import app.web.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserApiController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class UserApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

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
    void login_ShouldReturn200AndToken_WhenCredentialsAreValid() throws Exception {
        String jsonBody = """
                {
                    "username": "testUser",
                    "password": "password123"
                }
                """;

        when(userService.login("testUser", "password123")).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.user.username").value("testUser"))
                .andExpect(jsonPath("$.user.id").value(1));
    }

    @Test
    void login_ShouldReturn401_WhenCredentialsAreInvalid() throws Exception {
        String jsonBody = """
                {
                    "username": "testUser",
                    "password": "wrongPassword"
                }
                """;

        when(userService.login(anyString(), anyString()))
                .thenThrow(new AuthenticationException("The username or password is incorrect"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("The username or password is incorrect"));
    }

    @Test
    void logout_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/users/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out"));
    }

    @Test
    void register_ShouldReturn200_WhenRequestIsValid() throws Exception {
        String jsonBody = """
                {
                    "username": "newUser",
                    "password": "password123",
                    "passwordConfirm": "password123"
                }
                """;

        when(userService.register("newUser", "password123", User.Status.USER)).thenReturn(testUser);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Registered"));

        verify(userService).register("newUser", "password123", User.Status.USER);
    }

    @Test
    void register_ShouldReturn400_WhenPasswordsDoNotMatch() throws Exception {
        String jsonBody = """
                {
                    "username": "newUser",
                    "password": "password123",
                    "passwordConfirm": "differentPassword"
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("passwordConfirm"))
                .andExpect(jsonPath("$.message").value("Passwords do not match"));

        verify(userService, never()).register(anyString(), anyString(), any());
    }

    @Test
    void register_ShouldReturn400_WhenPasswordIsTooShort() throws Exception {
        String jsonBody = """
                {
                    "username": "newUser",
                    "password": "12",
                    "passwordConfirm": "12"
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("password"))
                .andExpect(jsonPath("$.message").value("Password should be at least 3 characters long"));
    }

    @Test
    void register_ShouldReturn400_WhenUsernameHasSpecialChars() throws Exception {
        String jsonBody = """
                {
                    "username": "new@user",
                    "password": "password123",
                    "passwordConfirm": "password123"
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("username"))
                .andExpect(jsonPath("$.message").value("Username can only contain letters, digits and underscore"));
    }


    @Test
    void me_ShouldReturn200AndUser() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.status").value("USER"));
    }

    @Test
    void updateProfile_ShouldReturn200_WhenRequestIsValid() throws Exception {
        String jsonBody = """
                {
                    "username": "updatedUser"
                }
                """;

        mockMvc.perform(patch("/api/users/me")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated profile"));

        verify(userService).update(eq(testUser), eq(1), eq("updatedUser"), isNull());
    }

    @Test
    void updateProfile_ShouldReturn400_WhenUsernameIsTooLong() throws Exception {
        String longUsername = "C".repeat(256);
        String jsonBody = """
                {
                    "username": "%s"
                }
                """.formatted(longUsername);

        mockMvc.perform(patch("/api/users/me")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("username"))
                .andExpect(jsonPath("$.message").value("Username is too long"));
    }


    @Test
    void changePassword_ShouldReturn200_WhenRequestIsValid() throws Exception {
        String jsonBody = """
                {
                    "currentPassword": "oldPassword",
                    "newPassword": "newValidPassword"
                }
                """;

        mockMvc.perform(patch("/api/users/password")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));

        verify(userService).changePassword(eq(testUser), eq(1), eq("oldPassword"), eq("newValidPassword"));
    }

    @Test
    void changePassword_ShouldReturn401_WhenCurrentPasswordIsWrong() throws Exception {
        String jsonBody = """
                {
                    "currentPassword": "wrongPassword",
                    "newPassword": "newValidPassword"
                }
                """;

        doThrow(new AuthenticationException("Current password is incorrect"))
                .when(userService).changePassword(any(), anyInt(), anyString(), anyString());

        mockMvc.perform(patch("/api/users/password")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    void changePassword_ShouldReturn400_WhenNewPasswordIsInvalid() throws Exception {
        String jsonBody = """
                {
                    "currentPassword": "oldPassword",
                    "newPassword": "12"
                }
                """;

        mockMvc.perform(patch("/api/users/password")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("newPassword"))
                .andExpect(jsonPath("$.message").value("Password should be at least 3 characters long"));
    }
}