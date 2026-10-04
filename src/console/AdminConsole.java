package console;

import domain.User;
import exception.AccessDeniedException;
import exception.AuthenticationException;
import exception.UserNotFoundException;
import exception.ValidationException;
import service.TaskService;
import service.UserService;

import java.util.List;
import java.util.Optional;

public class AdminConsole extends UserConsole {
    public AdminConsole(User user, TaskService taskService, UserService userService) {
        super(user, taskService, userService);
        this.userService = userService;
    }

    @Override
    public void parseCommand(String command, String query) {
        switch (command) {
            case "delusertasks" -> deleteUserTasks();
            default -> super.parseCommand(command, query);
        }
    }

    @Override
    protected void printHelp() {
        super.printHelp();
        System.out.println("-- ADMIN CONSOLE COMMANDS --");

        System.out.println("create task - Create a new task for a user");
        System.out.println("create user - Create a new user");
        System.out.println("display task - display user's tasks");
        System.out.println("display user - display all users");
        System.out.println("close - close anybody's task");
        System.out.println("delete user - delete a user");
        System.out.println("delclosed user - delete all closed tasks of a user");
        System.out.println("update task - update information about any task");
        System.out.println("update user - update a user's information");
        System.out.println("password - changed anybody's password");
        System.out.println("delusertasks - delete user's tasks");
    }

    @Override
    protected void create(String query) {
        if (!query.equalsIgnoreCase("user")) {
            super.create(query);
            return;
        }
        createUser();
    }

    private void createUser() {
        System.out.println("Enter username:");
        String username = readLine();

        System.out.println("Enter status:");
        System.out.println("0: User");
        System.out.println("1: Admin");
        User.Status status = switch (readInt().orElse(null)) {
            case 1 -> User.Status.ADMIN;
            case null, default -> User.Status.USER;
        };

        System.out.println("Enter the password: ");
        String password = readLine();
        System.out.println("Repeat the password: ");
        String passwordRepeat = readLine();

        if (!password.equals(passwordRepeat)) {
            System.out.println("The passwords are different");
            return;
        }

        try {
            userService.createNewUser(user, username, password, status);
            System.out.println("Successfully created a new user");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    protected void display(String query) {
        if (!query.equalsIgnoreCase("user")) {
            super.display(query);
            return;
        }
        List<User> users = userService.getUsers(user);
        for (User user : users) {
            System.out.println(user);
        }
    }

    @Override
    protected void delete(String query) {
        if (!query.equalsIgnoreCase("user")) {
            super.delete(query);
            return;
        }
        deleteUser();
    }

    private void deleteUser() {
        System.out.println("Enter the id of the user to delete:");
        Optional<Integer> optional = readInt();
        if (optional.isEmpty()) {
            System.out.println("It is not a number");
            return;
        }

        int userId = optional.get();
        try {
            userService.deleteUserByID(userId, user);
        } catch (AccessDeniedException e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Successfully deleted user with ID: " + userId);
    }

    @Override
    protected void update(String query) {
        if (!query.equalsIgnoreCase("user")) {
            super.update(query);
            return;
        }
        updateUser();
    }

    private void updateUser() {
        System.out.println("Enter the id of the user to update:");
        Optional<Integer> optional = readInt();
        if (optional.isEmpty()) {
            System.out.println("It is not a number");
            return;
        }

        int userId = optional.get();
        User userToUpdate;
        try {
            userToUpdate = userService.getById(userId, user);
        } catch (UserNotFoundException e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Username is: " + userToUpdate.getUsername());
        System.out.println("Enter new username:");
        String newUsername = readLine();

        System.out.println("Status is: " + userToUpdate.getStatus());
        System.out.println("Enter new status:");
        System.out.println("0: User");
        System.out.println("1: Admin");
        User.Status newStatus = switch (readInt().orElse(null)) {
            case 0 -> User.Status.USER;
            case 1 -> User.Status.ADMIN;
            case null, default -> null;
        };

        try {
            User newUser = userService.update(user, userId, newUsername, newStatus);
            if (newUser.getID() == user.getID()) {
                user = newUser;
            }
            System.out.println("Successfully updated the info");
        } catch (AccessDeniedException | ValidationException e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteUserTasks() {
        System.out.println("Enter the id of the user whose tasks to delete:");
        Optional<Integer> optional = readInt();
        if (optional.isEmpty()) {
            System.out.println("It is not a number");
            return;
        }

        int userId = optional.get();
        try {
            taskService.deleteTasksByUserId(userId, user);
        } catch (AccessDeniedException e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Successfully deleted tasks of user with ID: " + userId);
    }

    @Override
    protected void changePassword() {
        System.out.println("Enter the id of a user whose password to change:");
        Optional<Integer> optional = readInt();
        if (optional.isEmpty()) {
            System.out.println("The id is not a number");
            return;
        }

        int userId = optional.get();
        String password = "";
        if (userId == user.getID()) {
            System.out.println("Enter your password: ");
            password = readLine();
        }
        System.out.println("Enter new password: ");
        String newPassword = readLine();
        System.out.println("Repeat new password: ");
        String repeatPassword = readLine();
        if (!newPassword.equals(repeatPassword)) {
            System.out.println("The new password are different");
            return;
        }

        try {
            userService.changePassword(user, userId, password, newPassword);
            System.out.println("Changed the password successfully");
        } catch (AccessDeniedException | ValidationException | AuthenticationException | UserNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
