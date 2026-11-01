package app.service;

import app.domain.User;
import app.exception.AccessDeniedException;
import app.exception.AuthenticationException;
import app.exception.ValidationException;
import app.repo.UserRepository;
import app.web.dto.request.user.UserQueryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

        AuthenticationException exception = assertThrows(AuthenticationException.class, () ->
                userService.login(normalUser.getUsername(), "password"));

        assertEquals("The username or password is incorrect", exception.getMessage());
    }

    @Test
    void login_ShouldThrow_WhenPasswordIsIncorrect() {
        when(userRepo.getByUsername(normalUser.getUsername())).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.matches("fakePassword", normalUser.getPasswordHash())).thenReturn(false);

        AuthenticationException exception = assertThrows(AuthenticationException.class, () ->
                userService.login(normalUser.getUsername(), "fakePassword"));

        assertEquals("The username or password is incorrect", exception.getMessage());
    }

    @Test
    void login_ShouldReturnUser_WhenUsernameAndPasswordAreCorrect() {
        when(userRepo.getByUsername(normalUser.getUsername())).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.matches("truePassword", normalUser.getPasswordHash())).thenReturn(true);

        User returnedUser = userService.login(normalUser.getUsername(), "truePassword");

        assertNotNull(returnedUser);
        assertEquals(normalUser.getId(), returnedUser.getId());
        assertEquals(normalUser.getUsername(), returnedUser.getUsername());
        assertEquals(normalUser.getStatus(), returnedUser.getStatus());
    }

    @Test
    void register_ShouldThrow_WhenUsernameIsAlreadyTaken() {
        when(userRepo.getByUsername(normalUser.getUsername())).thenReturn(Optional.of(normalUser));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                userService.register(normalUser.getUsername(), "valid_password", User.Status.USER));

        assertEquals("Username: " + normalUser.getUsername() + " is already taken", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
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
    void register_WithAdminStatus_ShouldSucceed() {
        when(passwordEncoder.encode(anyString())).thenReturn("adminHash");

        userService.register("new_admin", "secret", User.Status.ADMIN);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        assertEquals(User.Status.ADMIN, captor.getValue().getStatus());
    }

    @Test
    void update_ShouldThrow_WhenNonAdminUpdatesAnotherUser() {
        when(userRepo.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () ->
                userService.update(normalUser, otherUser.getId(), "newName", null));

        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void update_ShouldThrow_WhenNonAdminTriesToChangeStatus() {
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));

        assertThrows(AccessDeniedException.class, () ->
                userService.update(normalUser, normalUser.getId(), null, User.Status.ADMIN));

        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void update_ShouldThrow_WhenAdminTriesToDemoteHimself() {
        when(userRepo.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                userService.update(adminUser, adminUser.getId(), null, User.Status.USER));

        assertEquals("Admins cannot change themselves to users", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void update_ShouldThrow_WhenNewUsernameIsTakenBySomeoneElse() {
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(userRepo.getByUsername(otherUser.getUsername())).thenReturn(Optional.of(otherUser));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                userService.update(normalUser, normalUser.getId(), otherUser.getUsername(), null));

        assertEquals("Username: " + otherUser.getUsername() + " is already taken", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void update_ShouldSucceed_WhenNonAdminUpdatesOwnUsername() {
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(userRepo.getByUsername("newName")).thenReturn(Optional.empty());

        userService.update(normalUser, normalUser.getId(), "newName", null);

        assertEquals("newName", normalUser.getUsername());
        assertEquals(User.Status.USER, normalUser.getStatus());
        verify(userRepo).save(normalUser);
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
    void update_ShouldSucceed_WhenAdminKeepsOwnAdminStatus() {
        when(userRepo.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

        userService.update(adminUser, adminUser.getId(), null, User.Status.ADMIN);

        assertEquals(User.Status.ADMIN, adminUser.getStatus());
        verify(userRepo).save(adminUser);
    }

    @Test
    void changePassword_ShouldThrow_WhenNonAdminChangesOthersPassword() {
        assertThrows(AccessDeniedException.class, () ->
                userService.changePassword(normalUser, otherUser.getId(), "current", "newPassword"));

        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void changePassword_ShouldThrow_WhenCurrentPasswordIsWrong() {
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.matches("wrongPassword", normalUser.getPasswordHash())).thenReturn(false);

        assertThrows(AuthenticationException.class, () ->
                userService.changePassword(normalUser, normalUser.getId(), "wrongPassword", "newPassword"));

        verify(userRepo, never()).save(any(User.class));
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

    @Test
    void changePassword_ShouldSucceed_WhenAdminChangesAnotherAdminPassword() {
        User otherAdmin = new User("admin2", User.Status.ADMIN, "otherHash");
        otherAdmin.setId(5);
        when(userRepo.findById(otherAdmin.getId())).thenReturn(Optional.of(otherAdmin));
        when(passwordEncoder.encode("newValidPassword")).thenReturn("newHashedPassword");

        userService.changePassword(adminUser, otherAdmin.getId(), null, "newValidPassword");

        assertEquals("newHashedPassword", otherAdmin.getPasswordHash());
        verify(userRepo).save(otherAdmin);
    }

    @Test
    void createNewUser_ShouldThrowAccessDenied_WhenRequesterIsNotAdmin() {
        assertThrows(AccessDeniedException.class, () ->
                userService.createNewUser(normalUser, "newUser", "password", User.Status.USER));

        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void createNewUser_ShouldCallRegister_WhenRequesterIsAdmin() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        userService.createNewUser(adminUser, "newUser", "password", User.Status.USER);

        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void getUsers_ShouldThrowAccessDenied_WhenRequesterIsNotAdmin() {
        UserQueryRequest query = new UserQueryRequest(0, 10, null, null);

        assertThrows(AccessDeniedException.class, () ->
                userService.getUsers(normalUser, query));

        verify(userRepo, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getUsers_ShouldReturnPage_WhenRequesterIsAdmin() {
        UserQueryRequest query = new UserQueryRequest(0, 10, null, null);
        when(userRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        Page<User> result = userService.getUsers(adminUser, query);

        assertNotNull(result);
        verify(userRepo).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deleteUserById_ShouldThrowAccessDenied_WhenRequesterIsNotAdmin() {
        assertThrows(AccessDeniedException.class, () ->
                userService.deleteUserById(normalUser, otherUser.getId()));

        verify(userRepo, never()).deleteById(anyInt());
    }

    @Test
    void deleteUserById_ShouldThrowAccessDenied_WhenAdminDeletesThemselves() {
        assertThrows(AccessDeniedException.class, () ->
                userService.deleteUserById(adminUser, adminUser.getId()));

        verify(userRepo, never()).deleteById(anyInt());
    }

    @Test
    void deleteUserById_ShouldDelete_WhenAdminDeletesOtherUser() {
        userService.deleteUserById(adminUser, otherUser.getId());
        verify(userRepo).deleteById(otherUser.getId());
    }

    @Test
    void deleteUserById_ShouldDelete_WhenAdminDeletesAnotherAdmin() {
        User otherAdmin = new User("admin2", User.Status.ADMIN, "hash");
        otherAdmin.setId(5);

        userService.deleteUserById(adminUser, otherAdmin.getId());

        verify(userRepo).deleteById(otherAdmin.getId());
    }
}