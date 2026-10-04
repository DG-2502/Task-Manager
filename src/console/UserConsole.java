package console;

import domain.Task;
import domain.User;
import exception.AccessDeniedException;
import exception.UserNotFoundException;
import service.TaskService;
import service.UserService;

import java.util.List;
import java.util.Optional;


public class UserConsole extends BasicConsole {
    protected User user;
    protected TaskService taskService;
    protected UserService userService;

    public UserConsole(User user, TaskService taskService, UserService userService) {
        this.user = user;
        this.taskService = taskService;
        this.userService = userService;
    }

    @Override
    public void parseCommand(String command, String query) {
        switch (command) {
            case "create" -> create(query);
            case "display" -> display(query);
            case "close" -> closeTask();
            case "delete" -> delete(query);
            case "delclosed" -> deleteClosed(query);
            case "update" -> update(query);
            case "password" -> changePassword();
            default -> super.parseCommand(command, query);
        }
    }

    @Override
    protected void printHelp() {
        super.printHelp();
        System.out.println("exit - Log out of the system");
        System.out.println("create - Create a new task");
        System.out.println("display - display tasks");
        System.out.println("close - close a task");
        System.out.println("delete - delete a task");
        System.out.println("delclosed - delete all closed tasks");
        System.out.println("update task - update information about a task");
        System.out.println("update info - update information about yourself");
        System.out.println("password - change your password");
    }

    protected void create(String query) {
        int userId = user.getID();
        if (query.equalsIgnoreCase("task") && user.isAdmin()) {
            System.out.println("Enter the id of a user whom to create a task:");
            Optional<Integer> optional = readInt();
            if (optional.isEmpty()) {
                System.out.println("The id should be a number");
                return;
            }

            userId = optional.get();
        }
        System.out.println("Enter the title of the task");
        String title = readLine();
        try {
            taskService.createTask(user, userId, title);
            System.out.println("Task was successfully created");
        } catch (AccessDeniedException | UserNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    protected void display(String query) {
        int userId = user.getID();
        if (query.equalsIgnoreCase("task") && user.isAdmin()) {
            System.out.println("Enter the id of a user whose tasks to display");
            Optional<Integer> optional = readInt();
            if (optional.isEmpty()) {
                System.out.println("The id should be a number");
                return;
            }

            userId = optional.get();
        }
        System.out.println("DISPLAY options:");
        System.out.println("0: All");
        System.out.println("1: Active");
        System.out.println("2: Closed");
        Optional<Integer> optional = readInt();
        int option = optional.orElse(0);

        TaskService.TaskFilter filter = switch (option) {
            case 1 -> TaskService.TaskFilter.ACTIVE;
            case 2 -> TaskService.TaskFilter.CLOSED;
            default -> TaskService.TaskFilter.ALL;
        };
        List<Task> tasks = taskService.getTasks(user, filter, userId);
        System.out.println("*** Tasks ***");
        for (Task task : tasks) {
            System.out.println(task);
        }
        if (tasks.isEmpty()) {
            System.out.println("no tasks");
        }
    }

    private void closeTask() {
        System.out.println("Enter the id of the task to close");
        Optional<Integer> optional = readInt();
        if (optional.isEmpty()) {
            System.out.println("Not a number");
            return;
        }

        int id = optional.get();
        try {
            taskService.closeTask(id, user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Task with id: " + id + " was closed");
    }

    protected void delete(String query) {
        System.out.println("Enter the id of the task to delete");
        Optional<Integer> optional = readInt();
        if (optional.isEmpty()) {
            System.out.println("Not a number");
            return;
        }

        int id = optional.get();
        try {
            taskService.deleteTask(user, id);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Task with id: " + id + " was deleted");
    }

    protected void deleteClosed(String query) {
        int userId = user.getID();
        if (query.equalsIgnoreCase("user") && user.isAdmin()) {
            System.out.println("Enter the id of a user whose tasks to display");
            Optional<Integer> optional = readInt();
            if (optional.isEmpty()) {
                System.out.println("The id should be a number");
                return;
            }

            userId = optional.get();
        }

        try {
            taskService.deleteClosed(user, userId);
            System.out.println("Successfully deleted all closed tasks");
        } catch (AccessDeniedException e) {
            System.out.println(e.getMessage());
        }
    }

    protected void update(String query) {
        switch (query) {
            case "task" -> updateTask();
            case "info" -> updateInfo();
            default -> System.out.println("Could update " + query + ". Can only update task/info");
        }
    }

    private void updateTask() {
        System.out.println("Enter the ID of a task to update");
        Optional<Integer> optional = readInt();
        if (optional.isEmpty()) {
            System.out.println("Not a number");
            return;
        }

        int taskId = optional.get();
        Task task;
        try {
            task = taskService.getById(taskId, user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        System.out.println("Title is: " + task.getTitle());
        System.out.println("Enter new title:");
        String newTitle = readLine();

        System.out.println("State is: " + task.getState());
        System.out.println("Enter new state:");
        System.out.println("0: Closed");
        System.out.println("1: Active");
        Task.State newState = switch (readInt().orElse(null)) {
            case 0 -> Task.State.CLOSED;
            case 1 -> Task.State.ACTIVE;
            case null, default -> null;
        };

        try {
            taskService.updateTask(user, taskId, newTitle, newState);
            System.out.println("Successfully updated the task");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void updateInfo() {
        System.out.println("Your username is: " + user.getUsername());
        System.out.println("Enter new username:");
        String newUsername = readLine();

        try {
            user = userService.update(user, user.getID(), newUsername, null);
            System.out.println("Successfully updated the info");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    protected void changePassword() {
        System.out.println("Enter your current password: ");
        String password = readLine();
        System.out.println("Enter new password: ");
        String newPassword = readLine();
        System.out.println("Repeat new password: ");
        String repeatPassword = readLine();

        if (!newPassword.equals(repeatPassword)) {
            System.out.println("The new password are different");
            return;
        }

        try {
            userService.changePassword(user, user.getID(), password, newPassword);
            System.out.println("Changed the password successfully");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
