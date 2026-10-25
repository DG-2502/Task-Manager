package app.service;

import app.domain.User;
import app.exception.AccessDeniedException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User normalUser;
    private User adminUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        normalUser = new User("user", User.Status.USER, "hash");
        normalUser.setId(1);

        adminUser = new User("admin", User.Status.ADMIN, "adminHash");
        adminUser.setId(2);

        otherUser = new User("other", User.Status.USER, "hash");
        otherUser.setId(3);
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
        when(passwordEncoder.matches(fakePassword, normalUser.getPasswordHash())).thenReturn(false);

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.login(normalUser.getUsername(), fakePassword);
        });

        assertEquals("The username or password is incorrect", exception.getMessage());
    }

    @Test
    void login_ShouldReturnUser_WhenUsernameAndPasswordAreCorrect() {
        String truePassword = "truePassword";

        when(userRepo.getByUsername(normalUser.getUsername())).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.matches(truePassword, normalUser.getPasswordHash())).thenReturn(true);

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

        assertEquals("Username: " + normalUser.getUsername() + " is already taken", exception.getMessage());
    }

    @Test
    void register_Success_WhenValidationsPass() {
        when(passwordEncoder.encode(anyString())).thenReturn("some hash");

        userService.register("new_user", "password", User.Status.USER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("new_user", savedUser.getUsername());
        assertEquals("some hash", savedUser.getPasswordHash());
        assertEquals(User.Status.USER, savedUser.getStatus());
    }


    @Test
    void update_ShouldThrow_WhenNonAdminUpdatesAnotherUser() {
        when(userRepo.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () -> {
            userService.update(normalUser, otherUser.getId(), "newName", null);
        });
    }

    @Test
    void update_ShouldThrow_WhenNonAdminTriesToChangeStatus() {
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));

        assertThrows(AccessDeniedException.class, () -> {
            userService.update(normalUser, normalUser.getId(), null, User.Status.ADMIN);
        });
    }

    @Test
    void update_ShouldThrow_WhenAdminTriesToDemoteHimself() {
        when(userRepo.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            userService.update(adminUser, adminUser.getId(), null, User.Status.USER);
        });

        assertEquals("Admins cannot change themselves to users", exception.getMessage());
    }

    @Test
    void update_ShouldThrow_WhenNewUsernameIsTakenBySomeoneElse() {
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(userRepo.getByUsername(otherUser.getUsername())).thenReturn(Optional.of(otherUser));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.update(normalUser, normalUser.getId(), otherUser.getUsername(), null);
        });

        assertEquals("Username: " + otherUser.getUsername() + " is already taken", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void update_ShouldSucceed_WhenAdminUpdatesAnotherUserStatus() {
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));

        userService.update(adminUser, normalUser.getId(), null, User.Status.ADMIN);

        assertEquals(User.Status.ADMIN, normalUser.getStatus());
        verify(userRepo, times(1)).save(normalUser);
    }

    @Test
    void update_ShouldSkipValidation_WhenUsernameIsUnchanged() {
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));

        userService.update(normalUser, normalUser.getId(), normalUser.getUsername(), null);

        verify(userRepo, never()).getByUsername(anyString());
        verify(userRepo, times(1)).save(normalUser);
    }


    @Test
    void changePassword_ShouldThrow_WhenNonAdminChangesOthersPassword() {
        assertThrows(AccessDeniedException.class, () -> {
            userService.changePassword(normalUser, otherUser.getId(), "current", "newPassword");
        });
    }

    @Test
    void changePassword_ShouldThrow_WhenCurrentPasswordIsWrong() {
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.matches("wrongPassword", normalUser.getPasswordHash())).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> {
            userService.changePassword(normalUser, normalUser.getId(), "wrongPassword", "newPassword");
        });

        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void changePassword_ShouldThrow_WhenNewPasswordIsInvalid() {
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.matches("correctPassword", normalUser.getPasswordHash())).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            userService.changePassword(normalUser, normalUser.getId(), "correctPassword", "12");
        });
    }

    @Test
    void changePassword_ShouldSucceed_WhenUserChangesTheirOwnPassword() {
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.matches("correctPassword", normalUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode("newValidPassword")).thenReturn("newHashedPassword");

        userService.changePassword(normalUser, normalUser.getId(), "correctPassword", "newValidPassword");

        assertEquals("newHashedPassword", normalUser.getPasswordHash());
        verify(userRepo, times(1)).save(normalUser);
    }

    @Test
    void changePassword_ShouldSucceed_WhenAdminChangesOthersPasswordWithoutCurrent() {
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.encode("newValidPassword")).thenReturn("newHashedPassword");

        userService.changePassword(adminUser, normalUser.getId(), null, "newValidPassword");

        assertEquals("newHashedPassword", normalUser.getPasswordHash());
        verify(userRepo, times(1)).save(normalUser);
    }
}
