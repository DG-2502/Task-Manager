package app.service;

import app.domain.User;
import app.exception.AccessDeniedException;
import app.exception.AuthenticationException;
import app.exception.UserNotFoundException;
import app.exception.ValidationException;
import app.repo.UserRepository;
import app.repo.UserSpecifications;
import app.web.dto.request.user.UserQueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(String username, String password) throws AuthenticationException {
        Optional<User> optionalUser = userRepo.getByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new AuthenticationException("The username or password is incorrect");
        }

        User user = optionalUser.get();
        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            return user;
        }

        throw new AuthenticationException("The username or password is incorrect");
    }

    public User register(String username, String password, User.Status status) throws ValidationException {
        validateUsernameUniqueness(username, null);

        String passwordHash = passwordEncoder.encode(password);

        User user = new User(username, status, passwordHash);
        return userRepo.save(user);
    }

    public User createNewUser(User requester, String username, String password, User.Status status) throws AccessDeniedException, ValidationException {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only admins can create new users");
        }

        return register(username, password, status);
    }

    private void validateUsernameUniqueness(String username, Integer excludedUserId) throws ValidationException {
        Optional<User> optionalUser = userRepo.getByUsername(username);
        if (optionalUser.isPresent()) {
            if (excludedUserId == null || optionalUser.get().getId() != excludedUserId) {
                throw new ValidationException("username", "Username: " + username + " is already taken");
            }
        }
    }

    public void update(User requester, int userId, String newUsername, User.Status newStatus) throws AccessDeniedException, ValidationException {
        User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
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

        if (newUsername != null && !newUsername.equals(user.getUsername())) {
            validateUsernameUniqueness(newUsername, userId);
            user.setUsername(newUsername);
        }

        user.setStatus(newStatus);
        userRepo.save(user);
    }

    public void changePassword(User requester, int userId, String password, String newPassword) throws AccessDeniedException, AuthenticationException, ValidationException, UserNotFoundException {
        if (!requester.isAdmin() && requester.getId() != userId) {
            throw new AccessDeniedException("Only admins can change others' password");
        }

        User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if (requester.getId() == userId) {
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new AuthenticationException("Current password is incorrect");
            }
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    public Page<User> getUsers(User requester, UserQueryRequest query) throws AccessDeniedException, ValidationException {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only admins can do that");
        }

        Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.by("id").ascending());

        Specification<User> spec = Specification.unrestricted();

        if (query.status() != null) {
            spec = spec.and(UserSpecifications.byStatus(query.status()));
        }
        if (query.search() != null && !query.search().isBlank())
            spec = spec.and(UserSpecifications.usernameContains(query.search()));

        return userRepo.findAll(spec, pageable);
    }

    public void deleteUserById(int id, User requester) throws AccessDeniedException {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only admins can do that");
        }
        if (id == requester.getId()) {
            throw new AccessDeniedException("Admins cannot delete themselves");
        }
        userRepo.deleteById(id);
    }

    public User getById(int id, User requester) throws UserNotFoundException {
        User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (!requester.isAdmin() && user.getId() != requester.getId()) {
            throw new UserNotFoundException(id);
        }

        return user;
    }
}
