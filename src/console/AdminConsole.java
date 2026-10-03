package console;

import domain.User;
import service.TaskService;
import service.UserService;

import java.util.List;

public class AdminConsole extends UserConsole {
    public AdminConsole(User user, TaskService taskService, UserService userService) {
        super(user, taskService, userService);
        this.userService = userService;
    }

    @Override
    public void parseCommand(String command, String query) {
        switch (command) {
            case "deluser" -> deleteUser();
            case "delusertasks" -> deleteUserTasks();
            case "listusers" -> listUsers();
            case "listtasks" -> listTasks();
            default -> super.parseCommand(command, query);
        }
    }

    @Override
    protected void printHelp() {
        super.printHelp();
        System.out.println("-- ADMIN CONSOLE COMMANDS --");
        System.out.println("deluser - delete a user");
        System.out.println("delusertasks - delete user's tasks");
        System.out.println("listusers - list all users in the database");
        System.out.println("listtasks - list all tasks of a user");
    }

    private void deleteUser() {
        System.out.println("Enter the id of the user to delete:");
        int userId = readInt(0, Integer.MAX_VALUE);
        try {
            userService.deleteUserByID(userId, user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Successfully deleted user with ID: " + userId);
    }

    private void deleteUserTasks() {
        System.out.println("Enter the id of the user whose tasks to delete:");
        int userId = readInt(0, Integer.MAX_VALUE);
        try {
            taskService.deleteTasksByUserId(userId, user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Successfully deleted tasks with user ID: " + userId);
    }

    private void listUsers() {
        List<User> users = userService.getUsers(user);
        for (User user : users) {
            System.out.println(user);
        }
    }

    private void listTasks() {
        System.out.println("Enter the id of the user whose tasks to display");
        int userId = readInt(0, Integer.MAX_VALUE);
        try {
            displayTasks(userId);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
