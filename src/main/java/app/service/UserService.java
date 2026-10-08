package app.service;

import app.domain.User;
import app.exception.AccessDeniedException;
import app.exception.AuthenticationException;
import app.exception.UserNotFoundException;
import app.exception.ValidationException;
import app.exception.*;
import org.springframework.stereotype.Service;
import app.repo.UserRepo;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepo userRepo;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepo userRepo, PasswordHasher passwordHasher) {
        this.userRepo = userRepo;
        this.passwordHasher = passwordHasher;
    }

    public User login(String username, String password) throws AuthenticationException {
        Optional<User> optionalUser = userRepo.getByUserName(username);
        if (optionalUser.isEmpty()) {
            throw new AuthenticationException("The username or password is incorrect");
        }

        User user = optionalUser.get();
        if (passwordHasher.matches(password, user.getPasswordHash())) {
            return user;
        }

        throw new AuthenticationException("The username or password is incorrect");
    }

    public void register(String username, String password, User.Status status) throws ValidationException {
        validatePassword(password);
        validateUsername(username);

        String passwordHash = passwordHasher.hash(password);

        User user = new User(username, status, passwordHash);
        userRepo.add(user);
    }

    public void createNewUser(User requester, String username, String password, User.Status status) throws AccessDeniedException {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only admins can create new users");
        }

        register(username, password, status);
    }

    private void validatePassword(String password) throws ValidationException {
        if (password == null || password.isBlank()) {
            throw new ValidationException("password", "Password is required");
        }
        if (!password.matches("\\w+")) {
            throw new ValidationException("password", "Password can only contain letters, digits and underscore");
        }
        if (password.length() < 3) {
            throw new ValidationException("password", "Password should be at least 3 characters long");
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("username", "Username is required");
        }
        if (!username.matches("\\w+")) {
            throw new ValidationException("username", "Username can only contain letters, digits and underscore");
        }
        Optional<User> optionalUser = userRepo.getByUserName(username);
        if (optionalUser.isPresent()) {
            throw new ValidationException("username", "Username is already taken");
        }
    }

    public User update(User requester, int userId, String newUsername, User.Status newStatus) throws AccessDeniedException, ValidationException {
        User user = userRepo.getById(userId);
        if (newStatus == null) {
            newStatus = user.getStatus();
        }

        boolean isStatusAdmin = newStatus == User.Status.ADMIN;
        if (!requester.isAdmin() && userId != requester.getId()) {
            throw new AccessDeniedException("Only admins can update other users");
        }

        if (!requester.isAdmin() && newStatus != user.getStatus()) {
            throw new AccessDeniedException("Only admins can change the admin status");
        }

        if (requester.isAdmin() && !isStatusAdmin && requester.getId() == userId) {
            throw new AccessDeniedException("Admins cannot change themselves to users");
        }

        if (newUsername != null && !newUsername.isBlank()) {
            Optional<User> optionalUser = userRepo.getByUserName(newUsername);
            if (optionalUser.isPresent() && optionalUser.get().getId() != userId) {
                throw new ValidationException("Username: " + newUsername + " is already taken");
            }
        }

        if (newUsername != null && !newUsername.isBlank()) user.setUsername(newUsername);
        user.setStatus(newStatus);
        userRepo.update(user);
        return user;
    }

    public void changePassword(User requester, int userId, String password, String newPassword) throws AccessDeniedException, AuthenticationException, ValidationException, UserNotFoundException {
        if (!requester.isAdmin() && requester.getId() != userId) {
            throw new AccessDeniedException("Only admins can change others' password");
        }

        User user = userRepo.getById(userId);

        if (requester.getId() == userId) {
            if (!passwordHasher.matches(password, user.getPasswordHash())) {
                throw new AuthenticationException("Current password is incorrect");
            }
        }

        validatePassword(newPassword);

        user.setPasswordHash(passwordHasher.hash(newPassword));
        userRepo.update(user);
    }

    public List<User> getUsers(User requester) {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only admins can do that");
        }
        return userRepo.get();
    }

    public void deleteUserByID(int ID, User requester) throws AccessDeniedException {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only admins can do that");
        }
        if (ID == requester.getId()) {
            throw new AccessDeniedException("Admins cannot delete themselves");
        }
        userRepo.delete(ID);
    }

    public User getById(int id, User requester) throws UserNotFoundException {
        User user = userRepo.getById(id);

        if (!requester.isAdmin() && user.getId() != requester.getId()) {
            throw new UserNotFoundException(id);
        }

        return user;
    }

    public boolean isEmpty() {
        return userRepo.isEmpty();
    }
}
