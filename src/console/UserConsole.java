package console;

import domain.Task;
import domain.User;
import service.TaskService;

import java.util.List;

public class UserConsole extends BasicConsole {
    User user;
    boolean createOption;
    boolean displayOption;
    boolean closeOption;
    boolean deleteOption;
    String query;

    TaskService taskService;

    public UserConsole(User user, TaskService taskService) {
        this.user = user;
        this.taskService = taskService;
    }

    @Override
    public boolean parseCommand(String command, String query) {
        this.query = query;
        return switch (command) {
            case "create" -> createOption = true;
            case "display" -> displayOption = true;
            case "close" -> closeOption = true;
            case "delete" -> deleteOption = true;
            default -> super.parseCommand(command, query);
        };
    }

    @Override
    public void executeCommands() {
        super.executeCommands();
        if (getHelpOption()) {
            System.out.println("exit - Log out of the system");
            System.out.println("create - Create a new task");
            System.out.println("display - display tasks");
            System.out.println("close - close an active task");
            System.out.println("delete optional<closed> - delete closed task\\-s");
            setHelpOption(false);
        }
        if (createOption) {
            createTask();
            createOption = false;
        }
        if (displayOption) {
            displayTasks();
            displayOption = false;
        }
        if (closeOption) {
            closeTask();
            closeOption = false;
        }
        if (deleteOption) {
            deleteTasks();
            deleteOption = false;
        }
    }

    private void createTask() {
        System.out.println("Create a new task");
        System.out.println("Enter the title of the task");
        String title = readName(true);
        taskService.createTask(title, user);
        System.out.println("Task was successfully created");
    }

    private void displayTasks() {
        System.out.println("DISPLAY options:");
        System.out.println("0: All");
        System.out.println("1: Active");
        System.out.println("2: Closed");
        int option = readInt(0, 2);
        List<Task> tasks = taskService.getTasks(option, user);
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

    private void deleteTasks() {
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
}
