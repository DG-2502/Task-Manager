package service;

import domain.User;
import exception.AuthenticationException;
import exception.ValidationException;
import repo.UserRepo;

import java.util.Optional;


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

    public void register(String username, String password, boolean isAdmin) throws ValidationException {
        Optional<User> optionalUser = userRepo.getByUserName(username);
        if (optionalUser.isPresent()) {
            throw new ValidationException("Username: " + username + " is already taken");
        }

        String passwordHash = passwordHasher.hash(password);

        User user = new User(username, isAdmin, passwordHash);
        userRepo.add(user);
    }
}
