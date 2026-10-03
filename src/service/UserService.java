package service;

import domain.User;
import exception.*;
import repo.TaskRepo;
import repo.UserRepo;

import java.util.List;
import java.util.Optional;


public class UserService {
    private final UserRepo userRepo;
    private final PasswordHasher passwordHasher;
    private final TaskRepo taskRepo;

    public UserService(UserRepo userRepo, PasswordHasher passwordHasher, TaskRepo taskRepo) {
        this.userRepo = userRepo;
        this.passwordHasher = passwordHasher;
        this.taskRepo = taskRepo;
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

    public void register(String username, String password, boolean isAdmin) throws ValidationException {
        if (password.length() < 3) {
            throw new ValidationException("Password should be at least 3 characters long");
        }

        Optional<User> optionalUser = userRepo.getByUserName(username);
        if (optionalUser.isPresent()) {
            throw new ValidationException("Username: " + username + " is already taken");
        }

        String passwordHash = passwordHasher.hash(password);

        User user = new User(username, isAdmin, passwordHash);
        userRepo.add(user);
    }

    public User update(User requester, User updatedUser) {
        User user = userRepo.getById(updatedUser.getID());

        if (!requester.isAdmin() && user.getID() != requester.getID()) {
            throw new AccessDeniedException("Only admins can update other users");
        }

        if (!requester.isAdmin() && user.isAdmin()) {
            throw new AccessDeniedException("Only admins can change the admin status");
        }

        if (requester.isAdmin() && !user.isAdmin() && requester.getID() == user.getID()) {
            throw new AccessDeniedException("Admins cannot change themselves to users");
        }

        Optional<User> optionalUser = userRepo.getByUserName(updatedUser.getUsername());
        if (optionalUser.isPresent() && optionalUser.get().getID() != updatedUser.getID()) {
            throw new ValidationException("Username: " + updatedUser.getUsername() + " is already taken");
        }

        userRepo.update(updatedUser);
        return userRepo.getById(updatedUser.getID());
    }

    public List<User> getUsers(User requester) {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only admins can do that");
        }
        return userRepo.get();
    }

    public void deleteUserByID(int ID, User requester) {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only admins can do that");
        }
        if (ID == requester.getID()) {
            throw new AccessDeniedException("Admins cannot delete themselves");
        }
        taskRepo.deleteByUserId(ID);
        userRepo.delete(ID);
    }

    public User getById(int id, User requester) {
        User user = userRepo.getById(id);

        if (!requester.isAdmin() && user.getID() != requester.getID()) {
            throw new UserNotFoundException(id);
        }

        return user;
    }

    public boolean isEmpty() {
        return userRepo.isEmpty();
    }
}
