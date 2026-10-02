package service;

import domain.User;
import exception.AccessDeniedException;
import exception.AuthenticationException;
import exception.ValidationException;
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

    public List<User> getUsers() {
        return userRepo.get();
    }

    public void deleteUserByID(int ID, User user) {
        if (!user.isAdmin()) {
            throw new AccessDeniedException("Only admins can do that");
        }
        taskRepo.deleteByUserId(ID);
        userRepo.delete(ID);
    }
}
