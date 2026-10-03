package console;

import domain.Task;
import domain.User;
import service.TaskService;
import service.UserService;

import java.util.List;

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
            case "create" -> createTask();
            case "display" -> displayTasks(user.getID());
            case "close" -> closeTask();
            case "delete" -> deleteTasks(query);
            case "update" -> update(query);
            default -> super.parseCommand(command, query);
        }
    }

    @Override
    protected void printHelp() {
        super.printHelp();
        System.out.println("exit - Log out of the system");
        System.out.println("create - Create a new task");
        System.out.println("display - display tasks");
        System.out.println("close - close an active task");
        System.out.println("delete [closed] - delete closed task\\-s");
        System.out.println("update task/info - update information about a task or yourself");
    }

    private void createTask() {
        System.out.println("Create a new task");
        System.out.println("Enter the title of the task");
        String title = readName(true);
        taskService.createTask(title, user);
        System.out.println("Task was successfully created");
    }

    protected void displayTasks(int userId) {
        System.out.println("DISPLAY options:");
        System.out.println("0: All");
        System.out.println("1: Active");
        System.out.println("2: Closed");
        int option = readInt(0, 2);
        TaskService.TaskFilter filter = switch (option) {
            case 1 -> TaskService.TaskFilter.ACTIVE;
            case 2 -> TaskService.TaskFilter.CLOSED;
            default -> TaskService.TaskFilter.ALL;
        };
        List<Task> tasks = taskService.getTasks(filter, userId);
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
        int id = readInt(0, Integer.MAX_VALUE);
        try {
            taskService.closeTask(id, user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Task with id: " + id + " was closed");
    }

    private void deleteTasks(String query) {
        if (query.equals("closed")) {
            taskService.deleteClosed(user);
            System.out.println("Deleted all closed tasks");
            return;
        }
        System.out.println("Enter the id of the task to delete");
        int id = readInt(0, Integer.MAX_VALUE);
        try {
            taskService.deleteTask(id, user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Task with id: " + id + " was deleted");
    }

    private void update(String query) {
        switch (query) {
            case "task" -> updateTask();
            case "info" -> updateInfo();
            default -> System.out.println("Could update " + query + ". Can only update task/info");
        }
    }

    private void updateTask() {
        System.out.println("Enter the ID of a task to update");
        int taskId = readInt(0, Integer.MAX_VALUE);
        Task task;

        try {
            task = taskService.getById(taskId, user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        System.out.println("Title is: " + task.getTitle());
        System.out.println("Enter new title:");
        String newTitle = readName(true);
        if (!newTitle.isEmpty()) {
            task.setTitle(newTitle);
        }

        try {
            taskService.updateTask(task, user);
            System.out.println("Successfully updated the task");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void updateInfo() {
        User updatedUser = userService.getById(user.getID(), user);

        System.out.println("Your username is: " + user.getUsername());
        System.out.println("Enter new username:");
        String newUsername = readName(true);

        if (!newUsername.isEmpty()) {
            updatedUser.setUsername(newUsername);
        }

        try {
            user = userService.update(user, updatedUser);
            System.out.println("Successfully updated the info");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
