package app.service;

import app.domain.User;
import app.exception.AuthenticationException;
import app.exception.ValidationException;
import app.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordHasher passwordHasher;

    @InjectMocks
    private UserService userService;

    private User normalUser;

    @BeforeEach
    void setUp() {
        normalUser = new User("user", User.Status.USER, "hash");
        normalUser.setId(1);
    }

    @Test
    void login_ShouldThrow_WhenUsernameDoesNotExist() {
        when(userRepo.getByUsername(normalUser.getUsername())).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.login(normalUser.getUsername(), "password");
        });

        assertEquals("The username or password is incorrect", exception.getMessage());
    }

    @Test
    void login_ShouldThrow_WhenPasswordIsIncorrect() {
        String fakePassword = "fakePassword";

        when(userRepo.getByUsername(normalUser.getUsername())).thenReturn(Optional.of(normalUser));
        when(passwordHasher.matches(fakePassword, normalUser.getPasswordHash())).thenReturn(false);

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.login(normalUser.getUsername(), fakePassword);
        });

        assertEquals("The username or password is incorrect", exception.getMessage());
    }

    @Test
    void login_ShouldReturnUser_WhenUsernameAndPasswordAreCorrect() {
        String truePassword = "truePassword";

        when(userRepo.getByUsername(normalUser.getUsername())).thenReturn(Optional.of(normalUser));
        when(passwordHasher.matches(truePassword, normalUser.getPasswordHash())).thenReturn(true);

        User returnedUser = userService.login(normalUser.getUsername(), truePassword);

        assertNotNull(returnedUser);
        assertEquals(returnedUser.getId(), normalUser.getId());
        assertEquals(returnedUser.getUsername(), normalUser.getUsername());
        assertEquals(returnedUser.getPasswordHash(), normalUser.getPasswordHash());
        assertEquals(returnedUser.getStatus(), normalUser.getStatus());
    }

    @Test
    void register_ShouldThrow_WhenPasswordIsEmpty() {
        String emptyPassword = "";

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register("some user", emptyPassword, User.Status.USER);
        });

        assertEquals("Password is required", exception.getMessage());
    }

    @Test
    void register_ShouldThrow_WhenPasswordDoesMatchRegex() {
        String invalidPassword = "@mypassword";

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register("some user", invalidPassword, User.Status.USER);
        });

        assertEquals("Password can only contain letters, digits and underscore", exception.getMessage());
    }

    @Test
    void register_ShouldThrow_WhenPasswordIsLessThanThreeCharacters() {
        String shortPassword = "12";

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register("some user", shortPassword, User.Status.USER);
        });

        assertEquals("Password should be at least 3 characters long", exception.getMessage());
    }

    @Test
    void register_ShouldThrow_WhenUsernameIsEmpty() {
        String emptyUsername = "";

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(emptyUsername, "valid_password", User.Status.USER);
        });

        assertEquals("Username is required", exception.getMessage());
    }

    @Test
    void register_ShouldThrow_WhenUsernameDoesMatchRegex() {
        String invalidUsername = "@mypassword";

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(invalidUsername, "valid_password", User.Status.USER);
        });

        assertEquals("Username can only contain letters, digits and underscore", exception.getMessage());
    }

    @Test
    void register_ShouldThrow_WhenUsernameIsAlreadyTaken() {
        when(userRepo.getByUsername(normalUser.getUsername())).thenReturn(Optional.of(normalUser));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(normalUser.getUsername(), "valid_password", User.Status.USER);
        });

        assertEquals("Username is already taken", exception.getMessage());
    }

    @Test
    void register_Success_WhenValidationsPass() {
        when(passwordHasher.hash(anyString())).thenReturn("some hash");

        userService.register("new_user", "password", User.Status.USER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("new_user", savedUser.getUsername());
        assertEquals("some hash", savedUser.getPasswordHash());
        assertEquals(User.Status.USER, savedUser.getStatus());
    }
}
